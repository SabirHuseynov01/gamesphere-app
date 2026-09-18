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

/**
 * Every active product in one request.
 *
 * The home page needs three slices of the catalog — top-ups, marketplace items
 * and whatever carries a discount — and they all come out of the same list, so
 * it reads it once and splits it locally instead of firing three queries.
 */
export async function getActiveCatalogProducts(signal) {
    const response = await httpClient.get("/products", {
        params: {
            status: "ACTIVE",
            page: 0,
            size: 200,
        },
        signal,
    });

    return response.data.data?.content || [];
}
