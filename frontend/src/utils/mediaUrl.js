const backendUrl =
    import.meta.env.VITE_BACKEND_URL || "http://localhost:8080";

export function resolveMediaUrl(url) {
    if (!url) {
        return null;
    }

    if (/^https?:\/\//i.test(url)) {
        return url;
    }

    const normalizedUrl = url.startsWith("/") ? url : `/${url}`;

    return `${backendUrl}${normalizedUrl}`;
}