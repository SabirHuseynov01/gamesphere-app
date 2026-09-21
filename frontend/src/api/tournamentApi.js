import httpClient from "./httpClient.js";

/**
 * Tournaments. The catalog reads are public; joining and leaving need a
 * session, and the server decides that — the UI only hides the buttons.
 */

export async function getTournaments(page = 0, size = 24, signal) {
    const response = await httpClient.get("/tournaments", {
        params: { page, size, sort: "startDate,asc" },
        signal,
    });
    return response.data.data;
}

export async function getTournament(id, signal) {
    const response = await httpClient.get(`/tournaments/${id}`, { signal });
    return response.data.data;
}

export async function getTournamentsByStatus(status, signal) {
    const response = await httpClient.get(`/tournaments/status/${status}`, { signal });
    return response.data.data;
}

export async function getUpcomingTournaments(limit = 6, signal) {
    const response = await httpClient.get("/tournaments/upcoming", {
        params: { limit },
        signal,
    });
    return response.data.data;
}

export async function getParticipants(id, signal) {
    const response = await httpClient.get(`/tournaments/${id}/participants`, { signal });
    return response.data.data;
}

/**
 * The server answers 404 when the signed-in user is not in the tournament,
 * which is an answer rather than a failure — so it resolves to null.
 */
export async function getMyParticipation(id, signal) {
    try {
        const response = await httpClient.get(`/tournaments/${id}/participants/me`, { signal });
        return response.data.data;
    } catch (error) {
        if (error.response?.status === 404) return null;
        throw error;
    }
}

export async function joinTournament(id, payload) {
    const response = await httpClient.post(`/tournaments/${id}/join`, payload);
    return response.data.data;
}

export async function leaveTournament(id) {
    await httpClient.delete(`/tournaments/${id}/leave`);
}

/** Any signed-in account may create one; the creator is taken from the token. */
export async function createTournament(payload) {
    const response = await httpClient.post("/tournaments", payload);
    return response.data.data;
}
