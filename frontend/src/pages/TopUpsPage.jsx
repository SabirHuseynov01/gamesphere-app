import { useEffect, useMemo, useState } from "react";
import { ArrowRight, Gamepad2 } from "lucide-react";
import { Link } from "react-router-dom";

import { getTopUpProducts } from "../api/topUpApi.js";
import { getApiErrorMessage } from "../api/httpClient.js";
import { useTranslation } from "../i18n/index.jsx";
import { resolveMediaUrl } from "../utils/mediaUrl.js";
import "../styles/top-ups.css";

function toSlug(value) {
    return value
        .toLowerCase()
        .trim()
        .replace(/[^a-z0-9]+/g, "-")
        .replace(/(^-|-$)/g, "");
}

function categoryForProduct(product) {
    if (product.productType === "GIFT_CARD") return "GIFT_CARD";
    if (product.productType === "SUBSCRIPTION") return "SUBSCRIPTION";
    if (["IN_GAME_CURRENCY", "CURRENCY"].includes(product.productType) && !product.gameTitle) return "DIGITAL_CREDIT";
    return "GAME_TOP_UPS";
}

export default function TopUpsPage() {
    const { t } = useTranslation();
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [category, setCategory] = useState("ALL");
    const categoryLabels = {
        ALL: t("topUps.categories.all"),
        GAME_TOP_UPS: t("topUps.categories.game"),
        GIFT_CARD: t("topUps.categories.gift"),
        SUBSCRIPTION: t("topUps.categories.subscription"),
        DIGITAL_CREDIT: t("topUps.categories.credit"),
    };

    useEffect(() => {
        const controller = new AbortController();

        getTopUpProducts(controller.signal)
            .then(setProducts)
            .catch((requestError) => {
                if (requestError.name !== "CanceledError") {
                    setError(getApiErrorMessage(requestError));
                }
            })
            .finally(() => {
                if (!controller.signal.aborted) {
                    setLoading(false);
                }
            });

        return () => controller.abort();
    }, []);

    const filteredProducts = useMemo(
        () => category === "ALL"
            ? products
            : products.filter((product) => categoryForProduct(product) === category),
        [category, products],
    );

    const games = useMemo(() => {
        const grouped = new Map();

        filteredProducts.forEach((product) => {
            const title = product.gameTitle || product.name || "Other top-ups";
            const current = grouped.get(title) || {
                title,
                imageUrl: product.imageUrl,
                products: [],
            };

            current.products.push(product);
            if (!current.imageUrl) current.imageUrl = product.imageUrl;
            grouped.set(title, current);
        });

        return [...grouped.values()];
    }, [filteredProducts]);

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
                        const imageUrl = resolveMediaUrl(game.imageUrl);
                        const firstProduct = game.products[0];

                        return (
                            <Link
                                className="top-up-game-card"
                                key={game.title}
                                to={`/top-ups/${toSlug(game.title)}`}
                                state={{ products: game.products }}
                            >
                                <div className="top-up-game-card__cover">
                                    {imageUrl ? (
                                        <img src={imageUrl} alt={`${game.title} cover`} />
                                    ) : (
                                        <Gamepad2 size={42} />
                                    )}
                                    <span>{game.products.length} packages</span>
                                </div>
                                <div className="top-up-game-card__content">
                                    <small>{t("topUps.title")}</small>
                                    <h2>{game.title}</h2>
                                    <p>
                                        {t("common.from")} {firstProduct.currency || "USD"} {firstProduct.finalPrice}
                                    </p>
                                    <ArrowRight size={18} />
                                </div>
                            </Link>
                        );
                    })}
                </section>
            )}
        </div>
    );
}
