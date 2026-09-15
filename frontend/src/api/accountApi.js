import httpClient from "./httpClient.js";

export async function login(email, password) {
    const response = await httpClient.post("/auth/login", { email, password });
    return response.data.data;
}

export async function register(payload) {
    const response = await httpClient.post("/auth/register", payload);
    return response.data.data;
}

export async function logout(refreshToken) {
    if (!refreshToken) return;
    await httpClient.post("/auth/logout", { refreshToken });
}

export async function getProfile(signal) {
    const response = await httpClient.get("/users/profile", { signal });
    return response.data.data;
}

export async function getCart(signal) {
    const response = await httpClient.get("/cart", { signal });
    return response.data.data;
}

export async function addCartItem(productId, quantity = 1, playerAccountId) {
    const response = await httpClient.post("/cart/items", {
        productId,
        quantity,
        ...(playerAccountId ? { playerAccountId } : {}),
    });
    return response.data.data;
}

export async function removeCartItem(productId) {
    const response = await httpClient.delete(`/cart/items/${productId}`);
    return response.data.data;
}

export async function clearCart() {
    await httpClient.delete("/cart/items");
}

export async function getWishlist(signal) {
    const response = await httpClient.get("/wishlist", { signal });
    return response.data.data;
}

export async function addWishlistItem(productId) {
    const response = await httpClient.post(`/wishlist/add/${productId}`);
    return response.data.data;
}

export async function removeWishlistItem(productId) {
    const response = await httpClient.delete(`/wishlist/remove/${productId}`);
    return response.data.data;
}

export async function getMyOrders(signal) {
    const response = await httpClient.get("/order/my", {
        params: { page: 0, size: 20 },
        signal,
    });
    return response.data.data?.content || [];
}

export async function updateProfile(payload) {
    const response = await httpClient.put("/users/profile", payload);
    return response.data.data;
}

export async function getNotifications(signal) {
    const response = await httpClient.get("/notifications", {
        params: { page: 0, size: 20 },
        signal,
    });
    return response.data.data || [];
}

export async function markNotificationRead(id) {
    const response = await httpClient.patch(`/notifications/${id}/read`);
    return response.data.data;
}

export async function createOrderFromCart() {
    const response = await httpClient.post("/order/from-cart");
    return response.data.data;
}

export async function createStripeCheckout(orderId) {
    const response = await httpClient.post("/payments/stripe/checkout", { orderId });
    return response.data.data;
}
