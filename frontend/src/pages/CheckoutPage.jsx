import { useState } from "react";
import { AlertTriangle, Check, CreditCard, Lock, ShieldCheck, ShoppingCart } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";

import { createOrderFromCart, createStripeCheckout } from "../api/accountApi.js";
import { getApiErrorMessage } from "../api/httpClient.js";
import { useAccount } from "../account/AccountProvider.jsx";
import { useMoney } from "../money/CurrencyProvider.jsx";
import { useTranslation } from "../i18n/index.jsx";
import "../styles/checkout.css";

/**
 * Only the methods the backend can actually take. Stripe Checkout is hosted by
 * Stripe, so no card field is ever rendered here — see README for why.
 */
const METHODS = [
    { key: "STRIPE_CARD", labelKey: "checkout.card", hintKey: "checkout.cardHint", available: true },
];

/**
 * One "we still need your in-game id" row. Keeps its own draft so typing in
 * one field does not re-render the whole cart summary.
 */
function PlayerIdField({ item, t, onSave }) {
    const [value, setValue] = useState("");
    const [saving, setSaving] = useState(false);
    const [failure, setFailure] = useState("");
    const label = item.playerIdLabel || t("topUps.playerId");

    async function handleSubmit(event) {
        event.preventDefault();
        setSaving(true);
        setFailure("");

        try {
            await onSave(item.productId, value.trim());
        } catch (requestError) {
            setFailure(getApiErrorMessage(requestError));
        } finally {
            setSaving(false);
        }
    }

    return (
        <form className="checkout-player-id" onSubmit={handleSubmit}>
            <label>
                <span>{item.productName}</span>
                <input
                    disabled={saving}
                    placeholder={label}
                    required
                    value={value}
                    onChange={(event) => setValue(event.target.value)}
                />
            </label>

            <button disabled={saving || !value.trim()} type="submit">
                <Check size={16} />
                {saving ? t("common.loading") : t("checkout.savePlayerId")}
            </button>

            {failure && <p className="checkout-player-id__error">{failure}</p>}
        </form>
    );
}

export default function CheckoutPage() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { basketsLoading, cart, cartCount, isAuthenticated, setCartPlayerId } = useAccount();
    const { formatPrice, isConverted, displayCurrency } = useMoney();
    const [method, setMethod] = useState(METHODS[0].key);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState("");

    const chargeCurrency = cart.currency || cart.items[0]?.currency || null;
    const converted = isConverted(chargeCurrency);

    // A top-up can be added before the shopper knows their in-game id, but the
    // order is refused while it is missing — so it has to be askable here.
    const missingPlayerIds = cart.items.filter((item) => item.requiresPlayerId && !item.playerAccountId);

    async function handlePay() {
        if (missingPlayerIds.length > 0) {
            setError(t("checkout.playerIdRequired"));
            return;
        }

        setBusy(true);
        setError("");

        try {
            const order = await createOrderFromCart();
            const checkout = await createStripeCheckout(order.id);

            if (!checkout?.checkoutUrl) {
                throw new Error(t("checkout.noUrl"));
            }

            // Leaves the SPA on purpose: the card form lives on Stripe's domain.
            window.location.assign(checkout.checkoutUrl);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError));
            setBusy(false);
        }
    }

    if (!isAuthenticated) {
        return (
            <div className="container checkout-page">
                <div className="checkout-guard">
                    <Lock size={28} />
                    <h1>{t("checkout.signInTitle")}</h1>
                    <p>{t("cart.signInFirst")}</p>
                </div>
            </div>
        );
    }

    // The cart arrives after mount, so an unloaded cart must not read as empty.
    if (basketsLoading) {
        return (
            <div className="container checkout-page">
                <div className="checkout-guard">{t("common.loading")}</div>
            </div>
        );
    }

    if (cart.items.length === 0) {
        return (
            <div className="container checkout-page">
                <div className="checkout-guard">
                    <ShoppingCart size={28} />
                    <h1>{t("cart.empty")}</h1>
                    <button type="button" onClick={() => navigate("/top-ups")}>
                        {t("nav.topUps")}
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="container checkout-page">
            <header className="checkout-page__header">
                <p className="section-kicker">{t("checkout.eyebrow")}</p>
                <h1>{t("checkout.title")}</h1>
            </header>

            <div className="checkout-shell">
                <div className="checkout-main">
                <section className="checkout-methods">
                    <p className="checkout-methods__notice">
                        <ShieldCheck size={18} />
                        {t("checkout.securedNotice")}
                    </p>

                    {METHODS.map((entry) => (
                        <label className="checkout-method" key={entry.key}>
                            <input
                                checked={method === entry.key}
                                name="payment-method"
                                type="radio"
                                value={entry.key}
                                onChange={() => setMethod(entry.key)}
                            />
                            <CreditCard size={20} />
                            <span>
                                <strong>{t(entry.labelKey)}</strong>
                                <small>{t(entry.hintKey)}</small>
                            </span>
                        </label>
                    ))}

                    <p className="checkout-methods__foot">{t("checkout.providerNote")}</p>
                </section>

                {missingPlayerIds.length > 0 && (
                    <section className="checkout-player-ids">
                        <p className="checkout-player-ids__notice">
                            <AlertTriangle size={18} />
                            {t("checkout.playerIdIntro")}
                        </p>

                        {missingPlayerIds.map((item) => (
                            <PlayerIdField
                                item={item}
                                key={item.productId}
                                t={t}
                                onSave={setCartPlayerId}
                            />
                        ))}
                    </section>
                )}
                </div>

                <aside className="checkout-summary">
                    <header>
                        <span>{t("checkout.cartTotal")}</span>
                        <strong>{formatPrice(cart.totalAmount, chargeCurrency)}</strong>
                    </header>
                    <p className="checkout-summary__count">{t("cart.count", { count: cartCount })}</p>

                    <ul className="checkout-summary__items">
                        {cart.items.map((item) => (
                            <li key={item.productId}>
                                <span>
                                    {item.productName}
                                    {item.playerAccountId && <small>{item.playerAccountId}</small>}
                                </span>
                                <span>{formatPrice(item.price, item.currency || chargeCurrency)} × {item.quantity}</span>
                            </li>
                        ))}
                    </ul>

                    <div className="checkout-summary__total">
                        <span>{t("cart.total")}</span>
                        <strong>{formatPrice(cart.totalAmount, chargeCurrency)}</strong>
                    </div>

                    {converted && (
                        <p className="checkout-summary__charge">
                            {t("checkout.chargedIn", {
                                display: displayCurrency,
                                currency: chargeCurrency,
                                amount: Number(cart.totalAmount).toFixed(2),
                            })}
                        </p>
                    )}

                    {error && <p className="checkout-summary__error">{error}</p>}

                    <button
                        className="checkout-summary__pay"
                        disabled={busy || missingPlayerIds.length > 0}
                        type="button"
                        onClick={handlePay}
                    >
                        {busy ? t("checkout.redirecting") : t("checkout.payWithCard")}
                    </button>

                    <p className="checkout-summary__fine">{t("checkout.stripeNote")}</p>
                    <Link className="checkout-summary__back" to="/top-ups">{t("checkout.keepShopping")}</Link>
                </aside>
            </div>
        </div>
    );
}
