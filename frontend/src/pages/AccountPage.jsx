import { useEffect, useState } from "react";
import { Bell, BadgeCheck, Heart, LogOut, Receipt, UserRound, Wallet } from "lucide-react";
import { Link, useParams } from "react-router-dom";

import {
    getMyOrders,
    getNotifications,
    getProfile,
    markNotificationRead,
    updateProfile,
} from "../api/accountApi.js";
import { getApiErrorMessage } from "../api/httpClient.js";
import { useAccount } from "../account/AccountProvider.jsx";
import { useTranslation } from "../i18n/index.jsx";
import { resolveMediaUrl } from "../utils/mediaUrl.js";
import { formatDateTime, formatEnum, formatMoney } from "../utils/formatters.js";
import "../styles/account.css";

const SECTIONS = ["profile", "orders", "wishlist", "notifications"];

const emptyForm = { firstName: "", lastName: "", phoneNumber: "", address: "", avatarUrl: "" };

function AccountAside({ active, notificationCount, onSignOut, profile, t, user, wishlistCount, orderCount }) {
    const avatarUrl = resolveMediaUrl(profile?.avatarUrl);
    const name = profile?.username || user?.username || "";

    const links = [
        { key: "profile", to: "/account", icon: <UserRound size={17} />, label: t("account.dashboard") },
        { key: "orders", to: "/account/orders", icon: <Receipt size={17} />, label: t("account.orders"), count: orderCount },
        { key: "wishlist", to: "/account/wishlist", icon: <Heart size={17} />, label: t("nav.wishlist"), count: wishlistCount },
        { key: "notifications", to: "/account/notifications", icon: <Bell size={17} />, label: t("account.notifications"), count: notificationCount },
    ];

    return (
        <aside className="account-side">
            <div className="account-identity">
                <span className="account-identity__avatar">
                    {avatarUrl
                        ? <img src={avatarUrl} alt="" />
                        : name.slice(0, 1).toUpperCase()}
                </span>
                <strong>{name}</strong>
                <small>{profile?.email || user?.email}</small>
                {(profile?.seller ?? profile?.isSeller) && (
                    <span className="account-identity__tag">
                        <BadgeCheck size={14} />
                        {t("account.seller")}
                    </span>
                )}
            </div>

            <div className="account-stats">
                <div>
                    <span><Wallet size={14} /> {t("account.balance")}</span>
                    <strong>{formatMoney(profile?.balance ?? 0, null)}</strong>
                </div>
                <div>
                    <span><Receipt size={14} /> {t("account.ordersShort")}</span>
                    <strong>{orderCount}</strong>
                </div>
            </div>

            <nav className="account-nav" aria-label={t("account.menu")}>
                {links.map((link) => (
                    <Link className={active === link.key ? "is-active" : ""} key={link.key} to={link.to}>
                        {link.icon}
                        {link.label}
                        {link.count > 0 && <em>{link.count}</em>}
                    </Link>
                ))}
                <button className="account-nav__signout" type="button" onClick={onSignOut}>
                    <LogOut size={17} />
                    {t("account.signOut")}
                </button>
            </nav>
        </aside>
    );
}

/** Keyed on the loaded profile, so the fields seed from props on mount. */
function ProfilePanel({ profile, t, onSaved }) {
    const [form, setForm] = useState(() => ({
        ...emptyForm,
        firstName: profile?.firstName || "",
        lastName: profile?.lastName || "",
        phoneNumber: profile?.phoneNumber || "",
        address: profile?.address || "",
        avatarUrl: profile?.avatarUrl || "",
    }));
    const [status, setStatus] = useState({ busy: false, message: "", error: "" });

    function update(field) {
        return (event) => setForm((current) => ({ ...current, [field]: event.target.value }));
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setStatus({ busy: true, message: "", error: "" });

        try {
            onSaved(await updateProfile(form));
            setStatus({ busy: false, message: t("account.saved"), error: "" });
        } catch (requestError) {
            setStatus({ busy: false, message: "", error: getApiErrorMessage(requestError) });
        }
    }

    return (
        <form className="account-form" onSubmit={handleSubmit}>
            <header className="account-panel__header">
                <div>
                    <h2>{t("account.profileTitle")}</h2>
                    <p>{t("account.profileText")}</p>
                </div>
            </header>

            <div className="account-form__grid">
                <label>
                    {t("account.username")}
                    <input disabled value={profile?.username || ""} />
                    <small>{t("account.readOnly")}</small>
                </label>
                <label>
                    {t("account.email")}
                    <input disabled value={profile?.email || ""} />
                    <small>{t("account.readOnly")}</small>
                </label>
                <label>
                    {t("account.firstName")}
                    <input value={form.firstName} onChange={update("firstName")} />
                </label>
                <label>
                    {t("account.lastName")}
                    <input value={form.lastName} onChange={update("lastName")} />
                </label>
                <label>
                    {t("account.phone")}
                    <input value={form.phoneNumber} onChange={update("phoneNumber")} />
                </label>
                <label>
                    {t("account.avatarUrl")}
                    <input value={form.avatarUrl} onChange={update("avatarUrl")} />
                </label>
                <label className="account-form__wide">
                    {t("account.address")}
                    <input value={form.address} onChange={update("address")} />
                </label>
            </div>

            {status.message && <p className="account-form__note is-success">{status.message}</p>}
            {status.error && <p className="account-form__note is-error">{status.error}</p>}

            <footer className="account-form__actions">
                <button disabled={status.busy} type="submit">
                    {status.busy ? t("account.working") : t("account.save")}
                </button>
            </footer>
        </form>
    );
}

export default function AccountPage() {
    const { t } = useTranslation();
    const { section = "profile" } = useParams();
    const { isAuthenticated, signOut, toggleWishlist, user, wishlist } = useAccount();
    const [profile, setProfile] = useState(null);
    const [orders, setOrders] = useState([]);
    const [notifications, setNotifications] = useState([]);
    // Nothing is fetched while signed out, so the page starts settled in that case.
    const [loading, setLoading] = useState(isAuthenticated);
    const [error, setError] = useState("");

    const active = SECTIONS.includes(section) ? section : "profile";
    const unread = notifications.filter((item) => item.status !== "READ").length;

    useEffect(() => {
        if (!isAuthenticated) return undefined;

        const controller = new AbortController();

        Promise.allSettled([
            getProfile(controller.signal),
            getMyOrders(controller.signal),
            getNotifications(controller.signal),
        ])
            .then(([profileResult, ordersResult, notificationsResult]) => {
                if (controller.signal.aborted) return;

                if (profileResult.status === "fulfilled") setProfile(profileResult.value);
                else setError(getApiErrorMessage(profileResult.reason));

                if (ordersResult.status === "fulfilled") setOrders(ordersResult.value);
                if (notificationsResult.status === "fulfilled") setNotifications(notificationsResult.value);
            })
            .finally(() => {
                if (!controller.signal.aborted) setLoading(false);
            });

        return () => controller.abort();
    }, [isAuthenticated]);

    async function readNotification(id) {
        const updated = await markNotificationRead(id);
        setNotifications((current) => current.map((item) => (item.id === id ? updated : item)));
    }

    if (!isAuthenticated) {
        return (
            <div className="container account-page">
                <div className="account-guard">
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
                <h1>{t("account.dashboard")}</h1>
            </header>

            <div className="account-shell">
                <AccountAside
                    active={active}
                    notificationCount={unread}
                    onSignOut={signOut}
                    orderCount={orders.length}
                    profile={profile}
                    t={t}
                    user={user}
                    wishlistCount={wishlist.length}
                />

                <section className="account-main">
                    {loading && <div className="account-empty">{t("common.loading")}</div>}
                    {!loading && error && <div className="account-empty is-error">{error}</div>}

                    {!loading && !error && active === "profile" && (
                        <ProfilePanel key={profile?.id} profile={profile} t={t} onSaved={setProfile} />
                    )}

                    {!loading && !error && active === "orders" && (
                        <>
                            <header className="account-panel__header">
                                <div>
                                    <h2>{t("account.orders")}</h2>
                                    <p>{t("account.ordersText")}</p>
                                </div>
                                <span>{orders.length}</span>
                            </header>

                            {orders.length === 0
                                ? <div className="account-empty">{t("account.noOrders")}</div>
                                : (
                                    <ul className="account-rows">
                                        {orders.map((order) => (
                                            <li key={order.id}>
                                                <span className="account-rows__main">
                                                    <strong>{order.orderNumber}</strong>
                                                    <small>{formatDateTime(order.createdAt)}</small>
                                                </span>
                                                <span className="account-tag">{formatEnum(order.status)}</span>
                                                <strong>{formatMoney(order.totalAmount, null)}</strong>
                                            </li>
                                        ))}
                                    </ul>
                                )}
                        </>
                    )}

                    {!loading && !error && active === "wishlist" && (
                        <>
                            <header className="account-panel__header">
                                <div>
                                    <h2>{t("nav.wishlist")}</h2>
                                    <p>{t("account.wishlistText")}</p>
                                </div>
                                <span>{wishlist.length}</span>
                            </header>

                            {wishlist.length === 0
                                ? <div className="account-empty">{t("wishlist.empty")}</div>
                                : (
                                    <ul className="account-rows">
                                        {wishlist.map((product) => (
                                            <li key={product.id}>
                                                <span className="account-rows__main">
                                                    <strong>{product.name}</strong>
                                                    <small>{product.gameTitle || formatEnum(product.productType)}</small>
                                                </span>
                                                <strong>{formatMoney(product.finalPrice, product.currency)}</strong>
                                                <button type="button" onClick={() => toggleWishlist(product.id)}>
                                                    {t("wishlist.remove")}
                                                </button>
                                            </li>
                                        ))}
                                    </ul>
                                )}
                        </>
                    )}

                    {!loading && !error && active === "notifications" && (
                        <>
                            <header className="account-panel__header">
                                <div>
                                    <h2>{t("account.notifications")}</h2>
                                    <p>{t("account.notificationsText")}</p>
                                </div>
                                <span>{t("account.unread", { count: unread })}</span>
                            </header>

                            {notifications.length === 0
                                ? <div className="account-empty">{t("account.noNotifications")}</div>
                                : (
                                    <ul className="account-rows">
                                        {notifications.map((item) => (
                                            <li className={item.status === "READ" ? "" : "is-unread"} key={item.id}>
                                                <span className="account-rows__main">
                                                    <strong>{item.title}</strong>
                                                    <small>{item.message}</small>
                                                </span>
                                                <span className="account-tag">{formatDateTime(item.createdAt)}</span>
                                                {item.status !== "READ" && (
                                                    <button type="button" onClick={() => readNotification(item.id)}>
                                                        {t("account.markRead")}
                                                    </button>
                                                )}
                                            </li>
                                        ))}
                                    </ul>
                                )}
                        </>
                    )}
                </section>
            </div>
        </div>
    );
}