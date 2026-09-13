import { ShoppingCart, X } from "lucide-react";
import { useState } from "react";

import { addTopUpToCart } from "../../../api/topUpApi.js";
import { getApiErrorMessage } from "../../../api/httpClient.js";
import { resolveMediaUrl } from "../../../utils/mediaUrl.js";
import { useTranslation } from "../../../i18n/index.jsx";

function productImage(product, game) {
    return resolveMediaUrl(product.imageUrl || game?.coverImageUrl || game?.coverUrl);
}

export default function TopUpPackagesSection({ game, products }) {
    const { t } = useTranslation();
    const [selectedProduct, setSelectedProduct] = useState(null);
    const [playerId, setPlayerId] = useState("");
    const [buyingId, setBuyingId] = useState(null);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    function openPurchase(product) {
        setSelectedProduct(product);
        setPlayerId("");
        setMessage("");
        setError("");
    }

    async function continuePurchase() {
        if (!selectedProduct) return;
        setBuyingId(selectedProduct.id);
        setMessage("");
        setError("");
        try {
            await addTopUpToCart(selectedProduct.id, playerId);
            setMessage(t("topUps.addedToCart"));
            setSelectedProduct(null);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError));
        } finally {
            setBuyingId(null);
        }
    }

    const requiresPlayerId = selectedProduct?.requiresPlayerId;
    const playerIdLabel = selectedProduct?.playerIdLabel || t("topUps.playerId");

    return <>
        <section className="top-up-detail-packages" aria-labelledby="top-up-packages-title">
            <div className="top-up-detail-packages__header">
                <div><p className="game-details__eyebrow">{t("topUps.packageEyebrow")}</p><h2 id="top-up-packages-title">{t("topUps.packagesTitle")}</h2></div>
                <span>{t("topUps.packageCount", { count: products.length })}</span>
            </div>
            {message && <div className="status-panel status-panel--success">{message}</div>}
            {error && <div className="status-panel status-panel--error">{error}</div>}
            <div className="top-up-package-grid">{products.map((product) => {
                const imageUrl = productImage(product, game);
                const finalPrice = product.finalPrice ?? product.discountPrice ?? product.price;
                return <article className="top-up-package-card" key={product.id}>
                    <div className="top-up-package-card__image">{imageUrl ? <img src={imageUrl} alt="" loading="lazy" /> : <ShoppingCart size={30} />}</div>
                    <small>{product.productType || t("topUps.gameTopUps")}</small>
                    <h3>{product.name}</h3>
                    <p>{product.inGameAmount ? `${product.inGameAmount} ${product.inGameCurrencyName || ""}` : t("topUps.digitalProduct")}{product.bonusAmount > 0 && ` +${product.bonusAmount} ${t("common.bonus")}`}</p>
                    {product.discountPrice != null && product.price != null && <del>{product.currency || "USD"} {product.price}</del>}
                    <strong>{product.currency || "USD"} {finalPrice}</strong>
                    <button type="button" onClick={() => openPurchase(product)} disabled={buyingId === product.id}><ShoppingCart size={16} />{t("topUps.buyNow")}</button>
                </article>;
            })}</div>
        </section>

        {selectedProduct && <div className="top-up-purchase-modal" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget) setSelectedProduct(null); }}>
            <div className="top-up-purchase-dialog" role="dialog" aria-modal="true" aria-labelledby="top-up-purchase-title">
                <button className="top-up-purchase-dialog__close" type="button" aria-label={t("common.close")} onClick={() => setSelectedProduct(null)}><X size={20} /></button>
                <p className="game-details__eyebrow">{t("topUps.confirmPurchase")}</p>
                <h2 id="top-up-purchase-title">{game?.title} - {selectedProduct.name}</h2>
                <p>{t("common.price")}: <strong>{selectedProduct.currency || "USD"} {selectedProduct.finalPrice ?? selectedProduct.discountPrice ?? selectedProduct.price}</strong></p>
                {requiresPlayerId && <label className="top-up-player-id">{playerIdLabel}<input autoFocus value={playerId} onChange={(event) => setPlayerId(event.target.value)} /></label>}
                <div className="top-up-purchase-dialog__actions"><button type="button" onClick={() => setSelectedProduct(null)}>{t("common.cancel")}</button><button type="button" onClick={continuePurchase} disabled={buyingId === selectedProduct.id || (requiresPlayerId && !playerId.trim())}>{t("topUps.continueBuy")}</button></div>
            </div>
        </div>}
    </>;
}