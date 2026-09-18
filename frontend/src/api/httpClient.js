import axios from "axios";

import {
    clearSession,
    readAccessToken,
    readRefreshToken,
    SESSION_EXPIRED_EVENT,
    storeTokens,
} from "./session.js";

const baseURL = import.meta.env.VITE_API_BASE_URL;

const httpClient = axios.create({
    baseURL,
    timeout: 10000,
    headers: {
        "Content-Type": "application/json",
    },
});

/**
 * Refreshing must not go through httpClient itself, or a failing refresh would
 * trip the same 401 handler and recurse.
 */
const refreshClient = axios.create({ baseURL, timeout: 10000 });

httpClient.interceptors.request.use((config) => {
    const token = readAccessToken();

    if (token && !config.headers.Authorization) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

/**
 * One refresh at a time. A page load fires several requests at once, and if
 * every 401 started its own refresh they would rotate the refresh token out
 * from under each other — only the first would survive.
 */
let refreshInFlight = null;

function refreshAccessToken() {
    if (refreshInFlight) return refreshInFlight;

    const refreshToken = readRefreshToken();

    if (!refreshToken) return Promise.reject(new Error("No refresh token stored."));

    refreshInFlight = refreshClient
        .post("/auth/refresh", { refreshToken })
        .then((response) => {
            const auth = response.data?.data;

            if (!auth?.accessToken) throw new Error("Refresh returned no access token.");

            storeTokens(auth);
            return auth.accessToken;
        })
        .finally(() => {
            refreshInFlight = null;
        });

    return refreshInFlight;
}

/** Auth calls own their own 401s; refreshing a failed sign-in makes no sense. */
function isAuthCall(config) {
    return (config?.url || "").startsWith("/auth/");
}

httpClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        const original = error.config;

        if (error.response?.status !== 401 || !original || original._retried || isAuthCall(original)) {
            return Promise.reject(error);
        }

        original._retried = true;

        try {
            const accessToken = await refreshAccessToken();

            original.headers = { ...original.headers, Authorization: `Bearer ${accessToken}` };

            return httpClient(original);
        } catch {
            // The refresh token is gone or rejected too: the session is over.
            clearSession();
            window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT));

            return Promise.reject(error);
        }
    },
);

export function getApiErrorMessage(error) {
    return (
        error.response?.data?.message ||
        error.message ||
        "An unexpected error occurred."
    );
}

export default httpClient;
