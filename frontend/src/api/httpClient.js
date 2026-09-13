import axios from "axios";

const httpClient = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 10000,
    headers: {
        "Content-Type": "application/json",
    },
});

httpClient.interceptors.request.use((config) => {
    const token = window.localStorage.getItem("accessToken")
        || window.localStorage.getItem("token")
        || window.localStorage.getItem("gamesphere-access-token");

    if (token && !config.headers.Authorization) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

export function getApiErrorMessage(error) {
    return (
        error.response?.data?.message ||
        error.message ||
        "An unexpected error occurred."
    );
}

export default httpClient;
