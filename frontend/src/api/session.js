/**
 * Where the session lives in localStorage, and the only place that knows it.
 *
 * Both the HTTP client (which refreshes an expired access token) and the
 * account context (which owns the signed-in user) read and write these keys,
 * so they are defined once rather than spelled out in each.
 */

export const ACCESS_TOKEN_KEY = "accessToken";
export const REFRESH_TOKEN_KEY = "gamesphere-refresh-token";
export const USER_KEY = "gamesphere-user";

/** Fired when a refresh fails, so the UI can drop back to a signed-out state. */
export const SESSION_EXPIRED_EVENT = "gamesphere:session-expired";

function read(key) {
    try {
        return window.localStorage.getItem(key);
    } catch {
        return null;
    }
}

export function readAccessToken() {
    return read(ACCESS_TOKEN_KEY)
        || read("token")
        || read("gamesphere-access-token");
}

export function readRefreshToken() {
    return read(REFRESH_TOKEN_KEY);
}

export function readStoredUser() {
    try {
        const raw = read(USER_KEY);
        return raw ? JSON.parse(raw) : null;
    } catch {
        return null;
    }
}

/**
 * Stores the tokens of an auth response. Refresh rotates both, so the new
 * refresh token has to land too — keeping the old one would make the next
 * refresh fail against a token the server has already revoked.
 */
export function storeTokens(auth) {
    window.localStorage.setItem(ACCESS_TOKEN_KEY, auth.accessToken);
    window.localStorage.setItem(REFRESH_TOKEN_KEY, auth.refreshToken || "");
}

export function storeSession(auth) {
    const user = { username: auth.username, email: auth.email };

    storeTokens(auth);
    window.localStorage.setItem(USER_KEY, JSON.stringify(user));

    return user;
}

export function clearSession() {
    window.localStorage.removeItem(ACCESS_TOKEN_KEY);
    window.localStorage.removeItem(REFRESH_TOKEN_KEY);
    window.localStorage.removeItem(USER_KEY);
}
