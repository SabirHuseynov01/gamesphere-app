import httpClient from "./httpClient.js";

export async function getCatalogProducts(section, signal) {
    const response = await httpClient.get("/products", {
        params: {
            catalogSection: section,
            status: "ACTIVE",
            page: 0,
            size: 100,
        },
        signal,
    });

    return response.data.data?.content || [];
}
