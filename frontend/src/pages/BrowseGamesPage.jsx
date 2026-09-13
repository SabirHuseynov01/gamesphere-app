import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";

import { searchGames } from "../api/gameApi.js";
import { getApiErrorMessage } from "../api/httpClient.js";
import { useTranslation } from "../i18n/index.jsx";
import ActiveFilterChips from "../features/games/components/ActiveFilterChips.jsx";
import BrowseFilters from "../features/games/components/BrowseFilters.jsx";
import EmptyCatalogState from "../features/games/components/EmptyCatalogState.jsx";
import GameCard from "../features/games/components/GameCard.jsx";
import "../features/games/browse-games.css";

const initialPage = {
    content: [],
    pageNumber: 0,
    pageSize: 12,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
};

const emptyFilters = {
    query: "",
    publisher: "",
    accessType: "",
    genres: "",
    platform: "",
    releaseYear: "",
    minPrice: "",
    maxPrice: "",
    sortBy: "ALPHABETICAL",
};

function readFilters(searchParams) {
    return {
        query: searchParams.get("q") || "",
        publisher: searchParams.get("publisher") || "",
        accessType: searchParams.get("accessType") || "",
        genres: searchParams.get("genres") || "",
        platform: searchParams.get("platform") || "",
        releaseYear: searchParams.get("releaseYear") || "",
        minPrice: searchParams.get("minPrice") || "",
        maxPrice: searchParams.get("maxPrice") || "",
        sortBy: searchParams.get("sortBy") || "ALPHABETICAL",
    };
}

function setIfPresent(searchParams, key, value) {
    if (value) {
        searchParams.set(key, value);
    }
}

export default function BrowseGamesPage() {
    const { t } = useTranslation();
    const [searchParams, setSearchParams] = useSearchParams();
    const [gamePage, setGamePage] = useState(initialPage);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const parameterKey = searchParams.toString();

    const filters = useMemo(
        () => readFilters(new URLSearchParams(parameterKey)),
        [parameterKey],
    );

    const requestedPage = Number(searchParams.get("page") || 0);

    useEffect(() => {
        const controller = new AbortController();

        async function loadGames() {
            try {
                setLoading(true);
                setError("");

                const result = await searchGames({
                    ...filters,
                    page: requestedPage,
                    size: 12,
                    signal: controller.signal,
                });

                setGamePage(result);
            } catch (requestError) {
                if (requestError.name !== "CanceledError") {
                    setError(getApiErrorMessage(requestError));
                }
            } finally {
                if (!controller.signal.aborted) {
                    setLoading(false);
                }
            }
        }

        loadGames();

        return () => controller.abort();
    }, [filters, requestedPage]);

    function applyFilters(nextFilters) {
        const nextParams = new URLSearchParams(searchParams);

        [
            "publisher",
            "accessType",
            "genres",
            "platform",
            "releaseYear",
            "minPrice",
            "maxPrice",
            "sortBy",
        ].forEach((key) => nextParams.delete(key));

        setIfPresent(nextParams, "publisher", nextFilters.publisher);
        setIfPresent(nextParams, "accessType", nextFilters.accessType);
        setIfPresent(nextParams, "genres", nextFilters.genres);
        setIfPresent(nextParams, "platform", nextFilters.platform);
        setIfPresent(nextParams, "releaseYear", nextFilters.releaseYear);
        setIfPresent(nextParams, "minPrice", nextFilters.minPrice);
        setIfPresent(nextParams, "maxPrice", nextFilters.maxPrice);

        if (nextFilters.sortBy !== "ALPHABETICAL") {
            nextParams.set("sortBy", nextFilters.sortBy);
        }

        nextParams.delete("page");
        setSearchParams(nextParams);
    }

    function resetBrowseFilters() {
        applyFilters(emptyFilters);
    }

    function removeFilter(key) {
        const nextParams = new URLSearchParams(searchParams);

        nextParams.delete(key);
        nextParams.delete("page");

        setSearchParams(nextParams);
    }

    function clearAllFilters() {
        setSearchParams(new URLSearchParams());
    }

    function changePage(page) {
        const nextParams = new URLSearchParams(searchParams);

        if (page === 0) {
            nextParams.delete("page");
        } else {
            nextParams.set("page", page);
        }

        setSearchParams(nextParams);
        window.scrollTo({
            top: 0,
            behavior: "smooth",
        });
    }

    const games = gamePage.content || [];

    return (
        <div className="container browse-page">
            <section className="browse-header">
                <div>
                    <p className="browse-header__eyebrow">{t("games.catalog")}</p>

                    <h1>
                        {filters.query
                            ? `Results for "${filters.query}"`
                            : t("games.browse")}
                    </h1>

                    <p>
                        {t("games.description")}
                    </p>
                </div>

                {!loading && !error && (
                    <strong className="result-count">
                        {t("games.showing", { count: games.length, total: gamePage.totalElements })}
                    </strong>
                )}
            </section>

            <div className="browse-layout">
                <BrowseFilters
                    key={parameterKey}
                    filters={filters}
                    onApply={applyFilters}
                    onReset={resetBrowseFilters}
                />

                <section className="browse-results">
                    <ActiveFilterChips
                        filters={filters}
                        onRemove={removeFilter}
                        onClearAll={clearAllFilters}
                    />

                    {loading && (
                        <div className="status-panel">{t("games.loading")}</div>
                    )}

                    {!loading && error && (
                        <div className="status-panel status-panel--error">
                            {error}
                        </div>
                    )}

                    {!loading && !error && games.length === 0 && (
                        <EmptyCatalogState onReset={clearAllFilters} />
                    )}

                    {!loading && !error && games.length > 0 && (
                        <>
                            <section className="game-grid" aria-label="Game catalog">
                                {games.map((game) => (
                                    <GameCard key={game.id} game={game} />
                                ))}
                            </section>

                            {gamePage.totalPages > 1 && (
                                <nav className="pagination" aria-label={t("games.pagination")}>
                                    <button
                                        type="button"
                                        disabled={gamePage.first}
                                        onClick={() =>
                                            changePage(gamePage.pageNumber - 1)
                                        }
                                    >
                                        {t("games.previous")}
                                    </button>

                                    <span>
                    {t("games.page", { current: gamePage.pageNumber + 1, total: gamePage.totalPages })}
                  </span>

                                    <button
                                        type="button"
                                        disabled={gamePage.last}
                                        onClick={() =>
                                            changePage(gamePage.pageNumber + 1)
                                        }
                                    >
                                        {t("games.next")}
                                    </button>
                                </nav>
                            )}
                        </>
                    )}
                </section>
            </div>
        </div>
    );
}
