import httpClient from "./httpClient.js";

/**
 * A user without a seller profile gets a 404 here, which is the normal state
 * for most accounts — it resolves to null so the page can offer the form.
 */
export async function getSellerProfile(signal) {
    try {
        const response = await httpClient.get("/seller/profile", { signal });
        return response.data.data;
    } catch (error) {
        if (error.response?.status === 404) return null;
        throw error;
    }
}

export async function createSellerProfile(payload) {
    const response = await httpClient.post("/seller/profile", payload);
    return response.data.data;
}
