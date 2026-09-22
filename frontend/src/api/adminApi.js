import httpClient from "./httpClient.js";

/**
 * Admin-only calls. Every one of these sits behind @PreAuthorize on the
 * server; the panel simply stops rendering controls the account cannot use.
 */

export async function findUserById(id, signal) {
    const response = await httpClient.get(`/admin/users/${id}`, { signal });
    return response.data.data;
}

export async function findUserByUsername(username, signal) {
    const response = await httpClient.get(
        `/admin/users/username/${encodeURIComponent(username)}`,
        { signal },
    );
    return response.data.data;
}

/** The endpoint adds the amount to the balance, so a negative value subtracts. */
export async function updateBalance(userId, amount) {
    const response = await httpClient.patch(`/admin/users/${userId}/balance`, null, {
        params: { amount },
    });
    return response.data.data;
}

export async function getOrdersByUser(userId, signal) {
    const response = await httpClient.get(`/admin/orders/user/${userId}`, { signal });
    return response.data.data;
}

export async function updateOrderStatus(orderId, status) {
    const response = await httpClient.patch(`/admin/orders/${orderId}/status`, { status });
    return response.data.data;
}

export async function approveReview(reviewId) {
    const response = await httpClient.patch(`/admin/reviews/${reviewId}/approve`);
    return response.data.data;
}

export async function approveSeller(sellerId) {
    const response = await httpClient.patch(`/admin/sellers/${sellerId}/approve`);
    return response.data.data;
}

export async function deactivateProduct(productId) {
    const response = await httpClient.patch(`/admin/products/${productId}/deactivate`);
    return response.data.data;
}

export async function cancelTournament(tournamentId) {
    const response = await httpClient.patch(`/admin/tournaments/${tournamentId}/cancel`);
    return response.data.data;
}

export async function disqualifyParticipant(tournamentId, userId) {
    const response = await httpClient.patch(
        `/admin/tournaments/${tournamentId}/participants/${userId}/disqualify`,
    );
    return response.data.data;
}

export async function getTopUpFulfillments(status, page = 0, size = 20, signal) {
    const response = await httpClient.get("/admin/top-up-fulfillments", {
        params: { status: status || undefined, page, size, sort: "createdAt,desc" },
        signal,
    });
    return response.data.data;
}

export async function updateFulfillmentStatus(id, payload) {
    const response = await httpClient.patch(`/admin/top-up-fulfillments/${id}/status`, payload);
    return response.data.data;
}
