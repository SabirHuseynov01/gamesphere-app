import httpClient from "./httpClient.js";

function getReleaseDateRange(releaseYear) {
    if (!releaseYear) {
        return {};
    }

    return {
        releaseDateFrom: `${releaseYear}-01-01`,
        releaseDateTo: `${releaseYear}-12-31`,
    };
}

export async function searchGames({
                                      query = "",
                                      publisher = "",
                                      accessType = "",
                                      genres = "",
                                      platform = "",
                                      releaseYear = "",
                                      minPrice = "",
                                      maxPrice = "",
                                      catalogType = "BOTH",
                                      sortBy = "ALPHABETICAL",
                                      page = 0,
                                      size = 12,
                                      signal,
                                  } = {}) {
    const response = await httpClient.get("/games/search", {
        params: {
            q: query || undefined,
            publisher: publisher || undefined,
            accessType: accessType || undefined,
            genres: genres || undefined,
            platform: platform || undefined,
            minOfferPrice: minPrice || undefined,
            maxOfferPrice: maxPrice || undefined,
            catalogType: catalogType || undefined,
            sortBy,
            page,
            size,
            ...getReleaseDateRange(releaseYear),
        },
        signal,
    });

    return response.data.data;
}

export async function getGameBySlug(slug, signal) {
    const response = await httpClient.get(`/games/slug/${slug}`, {
        signal,
    });

    return response.data.data;
}