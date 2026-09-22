import httpClient from "./httpClient.js";

/** Everything the signed-in user owns: keys, redemption links, entitlements. */

export async function getMyLibrary(signal) {
    const response = await httpClient.get("/library", { signal });
    return response.data.data;
}

export async function getLibraryItem(id, signal) {
    const response = await httpClient.get(`/library/${id}`, { signal });
    return response.data.data;
}
