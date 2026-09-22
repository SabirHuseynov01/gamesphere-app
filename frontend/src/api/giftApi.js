import httpClient from "./httpClient.js";

export async function getSentGifts(signal) {
    const response = await httpClient.get("/gifts/sent", { signal });
    return response.data.data;
}

export async function getReceivedGifts(signal) {
    const response = await httpClient.get("/gifts/received", { signal });
    return response.data.data;
}

export async function claimGift(token) {
    const response = await httpClient.post(`/gifts/claim/${token}`);
    return response.data.data;
}
