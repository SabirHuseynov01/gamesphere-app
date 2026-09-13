import httpClient from "./httpClient.js";

function toOptionalBoolean(value) {
    if (value === "") {
        return undefined;
    }

    return value === "true";
}

export async function getGameOffers(slug, filters = {}, signal) {
    const response = await httpClient.get(
        `/games/slug/${encodeURIComponent(slug)}/offers`,
        {
            params: {
                platform: filters.platform || undefined,
                productType: filters.productType || undefined,
                deliveryType: filters.deliveryType || undefined,
                officialStore: toOptionalBoolean(filters.officialStore),
                inStock: toOptionalBoolean(filters.inStock),
                sort: filters.sort || "PRICE_ASC",
            },
            signal,
        },
    );

    return response.data.data;
}