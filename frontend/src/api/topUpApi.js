import httpClient from "./httpClient.js";

const topUpTypes = new Set([
    "IN_GAME_CURRENCY",
    "IN_GAME_ITEM",
    "CURRENCY",
    "ITEM",
    "BATTLE_PASS",
    "GIFT_CARD",
    "SUBSCRIPTION",
]);

export async function getTopUpProducts(signal) {
    const response = await httpClient.get("/products", {
        params: {
            catalogSection: "TOP_UPS",
            status: "ACTIVE",
            page: 0,
            size: 100,
        },
        signal,
    });
    const products = response.data.data?.content || [];

    return products.filter((product) => topUpTypes.has(product.productType));
}

export async function addTopUpToCart(productId, playerAccountId) {
    const response = await httpClient.post("/cart/items", {
        productId,
        quantity: 1,
        ...(playerAccountId ? { playerAccountId } : {}),
    });
    return response.data.data;
}
