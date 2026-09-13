import { useEffect, useMemo, useState } from "react";
import { Gamepad2, Search, SlidersHorizontal } from "lucide-react";
import { Link } from "react-router-dom";

import { getTopUpGames, getTopUpProducts } from "../api/topUpApi.js";
import { getApiErrorMessage } from "../api/httpClient.js";
import { useTranslation } from "../i18n/index.jsx";
import { formatPlatform } from "../utils/formatters.js";
import { resolveMediaUrl } from "../utils/mediaUrl.js";
import { cheapestProduct, packageSummary, sortByPrice, toSlug } from "../utils/topUps.js";
import "../styles/top-ups.css";

const PREVIEW_PACKAGE_COUNT = 3;

function categoryForProduct(product) {
    if (product.productType === "GIFT_CARD") return "GIFT_CARD";
    if (product.productType === "SUBSCRIPTION") return "SUBSCRIPTION";
    if (["IN_GAME_CURRENCY", "CURRENCY"].includes(product.productType) && !product.gameTitle) return "DIGITAL_CREDIT";
    return "GAME_TOP_UPS";
}

const sorters = {
    ALPHABETICAL: (a, b) => a.title.localeCompare(b.title),
    PRICE_LOW: (a, b) => Number(a.fromPrice ?? 0) - Number(b.fromPrice ?? 0),
    PRICE_HIGH: (a, b) => Number(b.fromPrice ?? 0) - Number(a.fromPrice ?? 0),
    PACKAGES: (a, b) => b.products.length - a.products.length,
};

export default function TopUpsPage() {
    const { t } = useTranslation();
    const [products, setProducts] = useState([]);
    const [gamesById, setGamesById] = useState(new Map());
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [category, setCategory] = useState("ALL");
    const [query, setQuery] = useState("");
    const [platform, setPlatform] = useState("ALL");
    const [sortBy, setSortBy] = useState("ALPHABETICAL");

    const categoryLabels = {
        ALL: t("topUps.categories.all"),
        GAME_TOP_UPS: t("topUps.categories.game"),
        GIFT_CARD: t("topUps.categories.gift"),
        SUBSCRIPTION: t("topUps.categories.subscription"),
        DIGITAL_CREDIT: t("topUps.categories.credit"),
    };

    useEffect(() => {
        const controller = new AbortController();

        Promise.allSettled([
            getTopUpProducts(controller.signal),
            getTopUpGames(controller.signal),
        ])
            .then(([productsResult, gamesResult]) => {
                if (controller.signal.aborted) return;

                if (productsResult.status === "fulfilled") {
                    setProducts(productsResult.value);
                } else if (productsResult.reason?.name !== "CanceledError") {
                    setError(getApiErrorMessage(productsResult.reason));
                }

                // Cover art lives on the game, not the product. A failure here only
                // costs artwork, so the catalogue still renders without it.
                if (gamesResult.status === "fulfilled") {
                    setGamesById(new Map(gamesResult.value.map((game) => [game.id, game])));
                }
            })
            .finally(() => {
                if (!controller.signal.aborted) setLoading(false);
            });

        return () => controller.abort();
    }, []);

    const platforms = useMemo(
        () => [...new Set(products.map((product) => product.platform).filter(Boolean))].sort(),
        [products],
    );

    const games = useMemo(() => {
        const grouped = new Map();
        const search = query.trim().toLowerCase();

        products.forEach((product) => {
            if (category !== "ALL" && categoryForProduct(product) !== category) return;
            if (platform !== "ALL" && product.platform !== platform) return;

            const game = gamesById.get(product.gameId);
            const title = game?.title || product.gameTitle || product.name || "Other top-ups";

            if (search && !title.toLowerCase().includes(search)) return;

            const current = grouped.get(title) || {
                title,
                slug: game?.slug || toSlug(title),
                coverImageUrl: game?.coverImageUrl || product.imageUrl || null,
                products: [],
            };

            current.products.push(product);
            if (!current.coverImageUrl) current.coverImageUrl = product.imageUrl;
            grouped.set(title, current);
        });

        return [...grouped.values()]
            .map((game) => {
                const products = sortByPrice(game.products);
                const cheapest = cheapestProduct(products);

                return { ...game, products, fromPrice: cheapest?.finalPrice, currency: cheapest?.currency };
            })
            .sort(sorters[sortBy] || sorters.ALPHABETICAL);
    }, [category, gamesById, platform, products, query, sortBy]);

    return (
        <div className="container top-ups-page">
            <header className="top-ups-header">
                <div>
                    <p className="section-kicker">{t("topUps.eyebrow")}</p>
                    <h1>{t("topUps.title")}</h1>
                    <p>{t("topUps.description")}</p>
                </div>
                <strong>{t("topUps.available", { count: games.length })}</strong>
            </header>

            <nav className="catalog-category-nav" aria-label={t("topUps.title")}>
                {Object.keys(categoryLabels).map((value) => (
                    <button
                        className={category === value ? "is-active" : ""}
                        key={value}
                        type="button"
                        onClick={() => setCategory(value)}
                    >
                        {categoryLabels[value]}
                    </button>
                ))}
            </nav>

            <div className="top-ups-toolbar">
                <span className="top-ups-toolbar__title">
                    <SlidersHorizontal size={16} />
                    {t("games.filters")}
                </span>
                <label className="top-ups-toolbar__search">
                    <Search size={16} />
                    {/* No placeholder by request, so the field still needs a name for
                        screen readers and for the icon-only control to be understood. */}
                    <input
                        aria-label={t("topUps.searchPlaceholder")}
                        value={query}
                        onChange={(event) => setQuery(event.target.value)}
                    />
                </label>
                <label>
                    {t("games.platform")}
                    <select value={platform} onChange={(event) => setPlatform(event.target.value)}>
                        <option value="ALL">{t("games.allPlatforms")}</option>
                        {platforms.map((value) => (
                            <option key={value} value={value}>{formatPlatform(value)}</option>
                        ))}
                    </select>
                </label>
                <label>
                    {t("games.sortBy")}
                    <select value={sortBy} onChange={(event) => setSortBy(event.target.value)}>
                        <option value="ALPHABETICAL">{t("games.alphabetical")}</option>
                        <option value="PRICE_LOW">{t("games.lowHigh")}</option>
                        <option value="PRICE_HIGH">{t("games.highLow")}</option>
                        <option value="PACKAGES">{t("topUps.sortPackages")}</option>
                    </select>
                </label>
            </div>

            {loading && <div className="status-panel">{t("topUps.loading")}</div>}

            {!loading && error && (
                <div className="status-panel status-panel--error">{error}</div>
            )}

            {!loading && !error && games.length === 0 && (
                <div className="top-ups-empty">
                    <Gamepad2 size={32} />
                    <h2>{t("topUps.emptyTitle")}</h2>
                    <p>{t("topUps.emptyText")}</p>
                </div>
            )}

            {!loading && !error && games.length > 0 && (
                <section className="top-up-game-grid" aria-label={t("topUps.title")}>
                    {games.map((game) => {
                        const coverUrl = resolveMediaUrl(game.coverImageUrl);
                        const preview = game.products.slice(0, PREVIEW_PACKAGE_COUNT);
                        const remaining = game.products.length - preview.length;

                        return (
                            <Link
                                className="top-up-card"
                                key={game.title}
                                to={`/top-ups/${game.slug}`}
                                state={{ products: game.products, coverImageUrl: game.coverImageUrl, title: game.title }}
                            >
                                <div className="top-up-card__cover">
                                    {coverUrl ? (
                                        <img src={coverUrl} alt="" loading="lazy" />
                                    ) : (
                                        <span className="top-up-card__cover-fallback" aria-hidden="true">
                                            <Gamepad2 size={40} />
                                        </span>
                                    )}
                                    <span className="top-up-card__badge">{t("topUps.badge")}</span>
                                </div>

                                <div className="top-up-card__body">
                                    <h2>{game.title}</h2>
                                    <ul className="top-up-card__packages">
                                        {preview.map((product) => (
                                            <li key={product.id}>{packageSummary(product)}</li>
                                        ))}
                                        {remaining > 0 && (
                                            <li className="is-muted">{t("topUps.morePackages", { count: remaining })}</li>
                                        )}
                                    </ul>
                                    <footer className="top-up-card__footer">
                                        <span className="top-up-card__cta">{t("topUps.buyNow")}</span>
                                    </footer>
                                </div>
                            </Link>
                        );
                    })}
                </section>
            )}
        </div>
    );
}
