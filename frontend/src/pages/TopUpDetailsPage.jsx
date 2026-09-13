import { ArrowLeft, Gamepad2, ShoppingCart } from "lucide-react";
import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";

import { addTopUpToCart, getTopUpProducts } from "../api/topUpApi.js";
import { getApiErrorMessage } from "../api/httpClient.js";
import { resolveMediaUrl } from "../utils/mediaUrl.js";
import { useTranslation } from "../i18n/index.jsx";
import "../styles/top-ups.css";

export default function TopUpDetailsPage() {
    const { t } = useTranslation();
    const { slug } = useParams();
    const { state } = useLocation();
    const [products, setProducts] = useState(state?.products || []);
    const [loading, setLoading] = useState(!state?.products);
    const [playerId, setPlayerId] = useState("");
    const [buyingId, setBuyingId] = useState(null);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    useEffect(() => {
        if (state?.products) return undefined;

        const controller = new AbortController();

        getTopUpProducts(controller.signal)
            .then((allProducts) => {
                setProducts(
                    allProducts.filter((product) =>
                        product.gameTitle
                            ?.toLowerCase()
                            .trim()
                            .replace(/[^a-z0-9]+/g, "-")
                            .replace(/(^-|-$)/g, "") === slug,
                    ),
                );
            })
            .finally(() => {
                if (!controller.signal.aborted) setLoading(false);
            });

        return () => controller.abort();
    }, [slug, state?.products]);

    const title = products[0]?.gameTitle || slug.replaceAll("-", " ");
    const imageUrl = resolveMediaUrl(products[0]?.imageUrl);

    async function handleBuy(product) {
        setBuyingId(product.id);
        setMessage("");
        setError("");
        try {
            await addTopUpToCart(product.id, playerId);
            setMessage(t("topUps.addedToCart"));
        } catch (requestError) {
            setError(getApiErrorMessage(requestError));
        } finally {
            setBuyingId(null);
        }
    }

    return (
        <div className="container top-up-details-page">
            <Link className="back-link" to="/top-ups">
                <ArrowLeft size={17} />
                {t("nav.topUps")}
            </Link>

            <section className="top-up-details-hero">
                <div className="top-up-details-hero__cover">
                    {imageUrl ? <img src={imageUrl} alt={`${title} cover`} /> : <Gamepad2 size={48} />}
                </div>
                <div>
                    <p className="section-kicker">{t("topUps.packageEyebrow")}</p>
                    <h1>{title}</h1>
                    <p>{t("topUps.choose")}</p>
                </div>
            </section>

            {loading ? (
                <div className="status-panel">{t("topUps.loadingPackages")}</div>
            ) : products.length === 0 ? (
                <div className="top-ups-empty">
                    <h2>{t("topUps.openCatalog")}</h2>
                    <p>{t("topUps.packagesListed")}</p>
                </div>
            ) : (
                <>
                    {products.some((product) => product.requiresPlayerId) && (
                        <label className="top-up-player-id">
                            {products.find((product) => product.requiresPlayerId)?.playerIdLabel || t("topUps.playerId")}
                            <input value={playerId} onChange={(event) => setPlayerId(event.target.value)} />
                        </label>
                    )}
                    {message && <div className="status-panel status-panel--success">{message}</div>}
                    {error && <div className="status-panel status-panel--error">{error}</div>}
                    <section className="top-up-package-grid" aria-label={`${title} packages`}>
                        {products.map((product) => (
                            <article className="top-up-package-card" key={product.id}>
                                <small>{product.inGameCurrencyName || product.productType}</small>
                                <h2>
                                    {product.inGameAmount || product.name}
                                    {product.inGameCurrencyName && ` ${product.inGameCurrencyName}`}
                                </h2>
                                {product.bonusAmount > 0 && <p>{t("common.bonus")}: +{product.bonusAmount}</p>}
                                <strong>{product.currency || "USD"} {product.finalPrice}</strong>
                                <button type="button" onClick={() => handleBuy(product)} disabled={buyingId === product.id}>
                                    <ShoppingCart size={16} />
                                    {buyingId === product.id ? t("topUps.adding") : t("topUps.buyNow")}
                                </button>
                            </article>
                        ))}
                    </section>
                </>
            )}
        </div>
    );
}