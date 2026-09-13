import httpClient from "./httpClient.js";

export async function getApprovedGameReviews(gameId, signal) {
    const response = await httpClient.get(`/reviews/game/${gameId}/approved`, { signal });
    return response.data.data || [];
}

export async function createGameReview({ gameId, productId, rating, comment }) {
    const response = await httpClient.post("/reviews", { gameId, productId, rating: Number(rating), comment });
    return response.data.data;
}
