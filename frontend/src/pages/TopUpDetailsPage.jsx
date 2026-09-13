import { ArrowLeft, Gamepad2, ShoppingCart } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";

import { getTopUpGames, getTopUpProducts } from "../api/topUpApi.js";
import { useAccount } from "../account/AccountProvider.jsx";
import { getApiErrorMessage } from "../api/httpClient.js";
import { resolveMediaUrl } from "../utils/mediaUrl.js";
import { formatCurrency, formatPlatform } from "../utils/formatters.js";
import { packageDenomination, packageTier, sortByPrice, toSlug } from "../utils/topUps.js";
import { useTranslation } from "../i18n/index.jsx";
import "../styles/top-ups.css";

export default function TopUpDetailsPage() {
    const { t } = useTranslation();
    const { addToCart, isAuthenticated } = useAccount();
    const { slug } = useParams();
    const { state } = useLocation();
    const [products, setProducts] = useState(state?.products || []);
    const [game, setGame] = useState(
        state?.title ? { title: state.title, coverImageUrl: state.coverImageUrl } : null,
    );
    const [loading, setLoading] = useState(!state?.products);
    const [playerId, setPlayerId] = useState("");
    const [buyingId, setBuyingId] = useState(null);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    useEffect(() => {
        const controller = new AbortController();
        const needsProducts = !state?.products;

        Promise.allSettled([
            needsProducts ? getTopUpProducts(controller.signal) : Promise.resolve(state.products),
            getTopUpGames(controller.signal),
        ])
            .then(([productsResult, gamesResult]) => {
                if (controller.signal.aborted) return;

                const matchedGame = gamesResult.status === "fulfilled"
                    ? gamesResult.value.find((candidate) => candidate.slug === slug || toSlug(candidate.title) === slug)
                    : null;

                if (matchedGame) setGame(matchedGame);

                if (productsResult.status === "fulfilled") {
                    setProducts(
                        needsProducts
                            ? productsResult.value.filter((product) =>
                                (matchedGame && product.gameId === matchedGame.id)
                                || toSlug(product.gameTitle) === slug)
                            : productsResult.value,
                    );
                } else if (needsProducts && productsResult.reason?.name !== "CanceledError") {
                    setError(getApiErrorMessage(productsResult.reason));
                }
            })
            .finally(() => {
                if (!controller.signal.aborted) setLoading(false);
            });

        return () => controller.abort();
    }, [slug, state?.products, state?.title]);

    const sortedProducts = useMemo(() => sortByPrice(products), [products]);
    const title = game?.title || products[0]?.gameTitle || slug.replaceAll("-", " ");
    const coverUrl = resolveMediaUrl(game?.coverImageUrl || products[0]?.imageUrl);
    const platforms = useMemo(
        () => [...new Set(products.map((product) => product.platform).filter(Boolean))],
        [products],
    );
    const playerIdProduct = products.find((product) => product.requiresPlayerId);

    async function handleBuy(product) {
        setMessage("");
        setError("");

        // The cart endpoint is authenticated, so say that plainly instead of
        // letting the request come back as a bare 401.
        if (!isAuthenticated) {
            setError(t("cart.signInFirst"));
            return;
        }

        setBuyingId(product.id);
        try {
            await addToCart(product.id, playerId);
            setMessage(t("topUps.addedToCart"));
        } catch (requestError) {
            setError(getApiErrorMessage(requestError));
        } finally {
            setBuyingId(null);
        }
    }

    return (
        <div className="top-up-details-page">
            <section className={`top-up-hero${coverUrl ? "" : " top-up-hero--plain"}`}>
                {coverUrl && (
                    <img className="top-up-hero__backdrop" src={coverUrl} alt="" aria-hidden="true" />
                )}
                <div className="container top-up-hero__inner">
                    <Link className="back-link" to="/top-ups">
                        <ArrowLeft size={17} />
                        {t("nav.topUps")}
                    </Link>

                    <div className="top-up-hero__content">
                        <div className="top-up-hero__cover">
                            {coverUrl
                                ? <img src={coverUrl} alt={`${title} cover`} />
                                : <Gamepad2 size={44} aria-hidden="true" />}
                        </div>
                        <div className="top-up-hero__text">
                            <p className="section-kicker">{t("topUps.packageEyebrow")}</p>
                            <h1>{title}</h1>
                            <p>{t("topUps.choose")}</p>
                            <ul className="top-up-hero__meta">
                                <li>{t("topUps.packageCount", { count: products.length })}</li>
                                {platforms.map((value) => <li key={value}>{formatPlatform(value)}</li>)}
                            </ul>
                        </div>
                    </div>
                </div>
            </section>

            <div className="container top-up-details-body">
                {loading ? (
                    <div className="status-panel">{t("topUps.loadingPackages")}</div>
                ) : sortedProducts.length === 0 ? (
                    <div className="top-ups-empty">
                        <h2>{t("topUps.openCatalog")}</h2>
                        <p>{t("topUps.packagesListed")}</p>
                    </div>
                ) : (
                    <>
                        {playerIdProduct && (
                            <label className="top-up-player-id">
                                {playerIdProduct.playerIdLabel || t("topUps.playerId")}
                                <input
                                    placeholder={t("topUps.playerIdPlaceholder")}
                                    value={playerId}
                                    onChange={(event) => setPlayerId(event.target.value)}
                                />
                                <small>{t("topUps.playerIdHint")}</small>
                            </label>
                        )}

                        {message && <div className="status-panel status-panel--inline status-panel--success">{message}</div>}
                        {error && <div className="status-panel status-panel--inline status-panel--error">{error}</div>}

                        <section className="top-up-package-grid" aria-label={`${title} ${t("common.packages")}`}>
                            {sortedProducts.map((product, index) => {
                                const tier = packageTier(index, sortedProducts.length);
                                const imageUrl = resolveMediaUrl(product.imageUrl);
                                const discounted = Number(product.discountPercentage) > 0;
                                const denomination = packageDenomination(product);

                                return (
                                    <article className="top-up-package" key={product.id}>
                                        <div className={`top-up-package__tile top-up-package__tile--tier-${tier}`}>
                                            {/* Without artwork the denomination is the artwork, so it also
                                                carries the heading instead of repeating it underneath. */}
                                            {imageUrl
                                                ? <img src={imageUrl} alt="" loading="lazy" />
                                                : <h2 className="top-up-package__amount">{denomination}</h2>}
                                            {discounted && (
                                                <span className="top-up-package__flag">{t("topUps.specialOffer")}</span>
                                            )}
                                        </div>

                                        <div className="top-up-package__body">
                                            {imageUrl && <h2>{denomination}</h2>}
                                            <p className="top-up-package__bonus">
                                                {product.bonusAmount > 0
                                                    ? t("topUps.bonusAmount", { count: product.bonusAmount })
                                                    : t("topUps.noBonus")}
                                            </p>
                                            <div className="top-up-package__price">
                                                <strong>{formatCurrency(product.finalPrice, product.currency || "USD")}</strong>
                                                {discounted && (
                                                    <s>{formatCurrency(product.price, product.currency || "USD")}</s>
                                                )}
                                            </div>
                                            <button
                                                type="button"
                                                onClick={() => handleBuy(product)}
                                                disabled={buyingId === product.id}
                                            >
                                                <ShoppingCart size={16} />
                                                {buyingId === product.id ? t("topUps.adding") : t("topUps.purchase")}
                                            </button>
                                            <p className="top-up-package__terms">{t("topUps.terms")}</p>
                                        </div>
                                    </article>
                                );
                            })}
                        </section>
                    </>
                )}
            </div>
        </div>
    );
}
