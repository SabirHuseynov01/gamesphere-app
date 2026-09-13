import { useEffect, useState } from "react";
import { Heart, Receipt, UserRound } from "lucide-react";
import { Link, useParams } from "react-router-dom";

import { getMyOrders, getProfile } from "../api/accountApi.js";
import { getApiErrorMessage } from "../api/httpClient.js";
import { useAccount } from "../account/AccountProvider.jsx";
import { useTranslation } from "../i18n/index.jsx";
import { formatCurrency, formatDateTime, formatEnum } from "../utils/formatters.js";
import "../styles/account.css";

const SECTIONS = ["profile", "orders", "wishlist"];

export default function AccountPage() {
    const { t } = useTranslation();
    const { section = "profile" } = useParams();
    const { isAuthenticated, wishlist, toggleWishlist } = useAccount();
    const [profile, setProfile] = useState(null);
    const [orders, setOrders] = useState([]);
    // Nothing is fetched while signed out, so the page starts settled in that case.
    const [loading, setLoading] = useState(isAuthenticated);
    const [error, setError] = useState("");

    const active = SECTIONS.includes(section) ? section : "profile";

    useEffect(() => {
        if (!isAuthenticated) return undefined;

        const controller = new AbortController();

        Promise.allSettled([getProfile(controller.signal), getMyOrders(controller.signal)])
            .then(([profileResult, ordersResult]) => {
                if (controller.signal.aborted) return;

                if (profileResult.status === "fulfilled") setProfile(profileResult.value);
                if (ordersResult.status === "fulfilled") setOrders(ordersResult.value);

                const rejected = [profileResult, ordersResult].find((result) => result.status === "rejected");
                if (rejected && profileResult.status === "rejected") {
                    setError(getApiErrorMessage(rejected.reason));
                }
            })
            .finally(() => {
                if (!controller.signal.aborted) setLoading(false);
            });

        return () => controller.abort();
    }, [isAuthenticated]);

    if (!isAuthenticated) {
        return (
            <div className="container account-page">
                <div className="account-page__guard">
                    <UserRound size={30} />
                    <h1>{t("account.guardTitle")}</h1>
                    <p>{t("account.guardText")}</p>
                </div>
            </div>
        );
    }

    return (
        <div className="container account-page">
            <header className="account-page__header">
                <p className="section-kicker">{t("account.eyebrow")}</p>
                <h1>{profile?.username || t("account.dashboard")}</h1>
            </header>

            <nav className="account-page__tabs" aria-label={t("account.menu")}>
                <Link className={active === "profile" ? "is-active" : ""} to="/account">
                    <UserRound size={16} />
                    {t("account.dashboard")}
                </Link>
                <Link className={active === "orders" ? "is-active" : ""} to="/account/orders">
                    <Receipt size={16} />
                    {t("account.orders")}
                </Link>
                <Link className={active === "wishlist" ? "is-active" : ""} to="/account/wishlist">
                    <Heart size={16} />
                    {t("nav.wishlist")}
                </Link>
            </nav>

            {loading && <div className="status-panel">{t("common.loading")}</div>}
            {!loading && error && <div className="status-panel status-panel--error">{error}</div>}

            {!loading && !error && active === "profile" && (
                <dl className="account-facts">
                    <div><dt>{t("account.username")}</dt><dd>{profile?.username || "—"}</dd></div>
                    <div><dt>{t("account.email")}</dt><dd>{profile?.email || "—"}</dd></div>
                    <div><dt>{t("account.firstName")}</dt><dd>{profile?.firstName || "—"}</dd></div>
                    <div><dt>{t("account.lastName")}</dt><dd>{profile?.lastName || "—"}</dd></div>
                    <div><dt>{t("account.balance")}</dt><dd>{formatCurrency(profile?.balance ?? 0)}</dd></div>
                </dl>
            )}

            {!loading && !error && active === "orders" && (
                orders.length === 0
                    ? <div className="account-page__empty">{t("account.noOrders")}</div>
                    : (
                        <ul className="account-orders">
                            {orders.map((order) => (
                                <li key={order.id}>
                                    <span>
                                        <strong>{order.orderNumber}</strong>
                                        <small>{formatDateTime(order.createdAt)}</small>
                                    </span>
                                    <span className="account-orders__status">{formatEnum(order.status)}</span>
                                    <strong>{formatCurrency(order.totalAmount)}</strong>
                                </li>
                            ))}
                        </ul>
                    )
            )}

            {!loading && !error && active === "wishlist" && (
                wishlist.length === 0
                    ? <div className="account-page__empty">{t("wishlist.empty")}</div>
                    : (
                        <ul className="account-orders">
                            {wishlist.map((product) => (
                                <li key={product.id}>
                                    <span>
                                        <strong>{product.name}</strong>
                                        <small>{product.gameTitle || formatEnum(product.productType)}</small>
                                    </span>
                                    <strong>{formatCurrency(product.finalPrice, product.currency || "USD")}</strong>
                                    <button type="button" onClick={() => toggleWishlist(product.id)}>
                                        {t("wishlist.remove")}
                                    </button>
                                </li>
                            ))}
                        </ul>
                    )
            )}
        </div>
    );
}
