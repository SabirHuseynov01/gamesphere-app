import { useEffect, useRef, useState } from "react";
import { ArrowLeft, CalendarDays } from "lucide-react";
import { Link, useParams } from "react-router-dom";

import { getGameBySlug } from "../api/gameApi.js";
import { getGameOffers } from "../api/gameOfferApi.js";
import { getTopUpProducts } from "../api/topUpApi.js";
import { getApiErrorMessage } from "../api/httpClient.js";
import OfferFilters from "../features/offers/components/OfferFilters.jsx";
import OfferList from "../features/offers/components/OfferList.jsx";
import { resolveMediaUrl } from "../utils/mediaUrl.js";
import { useTranslation } from "../i18n/index.jsx";
import MediaImage from "../components/MediaImage.jsx";
import { getGameArtwork } from "../utils/gameArtwork.js";
import ReviewSection from "../features/reviews/ReviewSection.jsx";
import TopUpPackagesSection from "../features/topups/components/TopUpPackagesSection.jsx";
import "../features/offers/offers.css";

const initialFilters = {
    platform: "",
    productType: "",
    officialStore: "",
    inStock: "",
    sort: "PRICE_ASC",
};

export default function GameDetailsPage() {
    const { t } = useTranslation();
    const { slug } = useParams();

    const [comparison, setComparison] = useState(null);
    const [filters, setFilters] = useState(initialFilters);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [error, setError] = useState("");
    const [descriptionExpanded, setDescriptionExpanded] = useState(false);
    const [selectedEdition, setSelectedEdition] = useState("");
    const [topUpProducts, setTopUpProducts] = useState([]);
    const loadedSlugRef = useRef(null);

    useEffect(() => {
        const controller = new AbortController();
        const initialLoad = loadedSlugRef.current !== slug;

        async function loadComparison() {
            try {
                setError("");

                if (initialLoad) {
                    setLoading(true);
                } else {
                    setRefreshing(true);
                }

                const game = await getGameBySlug(slug, controller.signal);
                const [topUpResult, offersResult] = await Promise.allSettled([
                    getTopUpProducts(controller.signal),
                    getGameOffers(slug, filters, controller.signal),
                ]);
                const allTopUps = topUpResult.status === "fulfilled" ? topUpResult.value : [];
                const gameTopUps = allTopUps.filter((product) => product.gameId === game.id || product.gameTitle === game.title);
                const isTopUp = game.catalogType === "TOP_UP" || gameTopUps.length > 0;
                const result = isTopUp ? { game, offers: [], offerCount: 0, availablePlatforms: [] } : offersResult.status === "fulfilled" ? offersResult.value : (() => { throw offersResult.reason; })();

                loadedSlugRef.current = slug;
                setTopUpProducts(gameTopUps);
                setComparison(result);
            } catch (requestError) {
                if (requestError.name !== "CanceledError") {
                    setError(getApiErrorMessage(requestError));
                }
            } finally {
                if (!controller.signal.aborted) {
                    setLoading(false);
                    setRefreshing(false);
                }
            }
        }

        loadComparison();

        return () => controller.abort();
    }, [slug, filters]);

    function resetFilters() {
        setFilters(initialFilters);
    }

    useEffect(() => {
        if (comparison?.game?.title) {
            document.title = `${comparison.game.title} | GameSphere`;
        }
    }, [comparison?.game?.title]);

    if (loading && !comparison) {
        return <div className="container status-panel">{t("details.loading")}</div>;
    }

    if (error && !comparison) {
        return (
            <div className="container status-panel status-panel--error">
                {error}
            </div>
        );
    }

    const game = comparison.game;
    const coverUrl = resolveMediaUrl(getGameArtwork(game, "cover"));
    const backgroundUrl = resolveMediaUrl(getGameArtwork(game, "background"));
    const description = game.description || t("details.noDescription");
    const isLongDescription = description.length > 280;
    const visibleDescription = !descriptionExpanded && isLongDescription
        ? `${description.slice(0, 280).trim()}...`
        : description;
    const genres = Array.isArray(game.genres) ? game.genres : (game.genre ? [game.genre] : []);
    const platforms = game.supportedPlatforms || game.platforms || [];
    const isTopUpGame = comparison.game.catalogType === "TOP_UP" || topUpProducts.length > 0;
    const allOffers = comparison.offers || [];
    const editions = [...new Set(allOffers
        .filter((offer) => offer.productType === "GAME")
        .map((offer) => offer.editionName?.trim())
        .filter(Boolean))];
    const activeEdition = editions.includes(selectedEdition) ? selectedEdition : "";
    const selectedOffers = allOffers.filter((offer) =>
        offer.productType === "GAME" && (!activeEdition || offer.editionName === activeEdition));
    const additionalProductTypes = new Set([
        "DLC",
        "EXPANSION",
        "BUNDLE",
        "ADD_ON",
        "BATTLE_PASS",
        "ITEM",
        "IN_GAME_ITEM",
    ]);
    const additionalOffers = allOffers.filter((offer) => additionalProductTypes.has(offer.productType));
    const comparisonForSelectedEdition = {
        ...comparison,
        offers: selectedOffers,
        offerCount: selectedOffers.length,
        availablePlatforms: [...new Set(selectedOffers.map((offer) => offer.platform).filter(Boolean))],
    };
    const additionalComparison = {
        ...comparison,
        offers: additionalOffers,
        offerCount: additionalOffers.length,
        availablePlatforms: [...new Set(additionalOffers.map((offer) => offer.platform).filter(Boolean))],
    };
    return (
        <div className="container game-details">
            <Link className="back-link" to="/games">
                <ArrowLeft size={17} />
                {t("details.games")}
            </Link>

            <section className="game-details__content" style={backgroundUrl ? { "--game-background": `url(${backgroundUrl})` } : undefined}>
                <div className="game-details__cover">
                    <MediaImage src={coverUrl} alt={`${game.title} cover`} fallbackLabel={game.title} className="game-artwork" />
                </div>

                <div className="game-details__information">
                    <p>{game.developer || t("details.unknownDeveloper")}</p>
                    <h1>{game.title}</h1>

                    <div className="game-details__metadata">
                        <span>{game.publisher || t("details.unknownPublisher")}</span>

                        {game.releaseDate && (
                            <span>
                <CalendarDays size={16} />
                                {game.releaseDate}
                            </span>
                        )}
                        {genres.map((genre) => <span key={genre}>{genre}</span>)}
                        {game.accessType && <span>{game.accessType}</span>}
                        {platforms.map((platform) => <span key={platform}>{platform}</span>)}
                    </div>

                    <p className="game-details__description">
                        {visibleDescription}
                    </p>
                    {isLongDescription && <button type="button" className="game-details__read-more" onClick={() => setDescriptionExpanded((expanded) => !expanded)}>{descriptionExpanded ? t("details.readLess") : t("details.readMore")}</button>}
                </div>
            </section>

            <section className="game-details__overview">
                <p className="game-details__eyebrow">{t("details.overview")}</p>
                <h2>{game.title}</h2>
                <p>{description}</p>
            </section>

            {isTopUpGame && topUpProducts.length > 0 && <TopUpPackagesSection game={game} products={topUpProducts} />}

            {!isTopUpGame && editions.length > 0 && (
                <section className="edition-selector" aria-label={t("details.editions")}>
                    <div>
                        <p className="game-details__eyebrow">{t("details.editions")}</p>
                        <h2>{t("details.chooseEdition")}</h2>
                    </div>
                    <div className="edition-selector__options">
                        <button type="button" className={!activeEdition ? "is-active" : ""} onClick={() => setSelectedEdition("")}>{t("details.allEditions")}</button>
                        {editions.map((edition) => (
                            <button type="button" className={activeEdition === edition ? "is-active" : ""} key={edition} onClick={() => setSelectedEdition(edition)}>{edition}</button>
                        ))}
                    </div>
                </section>
            )}

            {!isTopUpGame && <OfferFilters
                filters={filters}
                onChange={setFilters}
                onReset={resetFilters}
            />
            }

            {error && (
                <div className="status-panel status-panel--error">
                    {error}
                </div>
            )}

            {!isTopUpGame && refreshing && (
                <p className="offers-refreshing">{t("details.refreshing")}</p>
            )}

            {!isTopUpGame && !error && <OfferList comparison={comparisonForSelectedEdition} />}

            {!isTopUpGame && !error && additionalOffers.length > 0 && (
                <section className="additional-content-section">
                    <OfferList comparison={additionalComparison} title={t("details.additionalContent")} />
                </section>
            )}

            {!error && <ReviewSection gameId={game.id} productId={selectedOffers[0]?.productId} />}
        </div>
    );
}
