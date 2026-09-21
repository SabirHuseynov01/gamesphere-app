import { useCallback, useEffect, useState } from "react";
import {
    BadgeCheck,
    PackageSearch,
    Receipt,
    Search,
    ShieldAlert,
    Wallet,
} from "lucide-react";

import {
    approveReview,
    approveSeller,
    cancelTournament,
    deactivateProduct,
    findUserById,
    findUserByUsername,
    getOrdersByUser,
    getTopUpFulfillments,
    updateBalance,
    updateFulfillmentStatus,
    updateOrderStatus,
} from "../api/adminApi.js";
import { getApiErrorMessage } from "../api/httpClient.js";
import { useAccount } from "../account/AccountProvider.jsx";
import { useTranslation } from "../i18n/index.jsx";
import { formatDateTime, formatEnum, formatMoney } from "../utils/formatters.js";
import "../styles/admin.css";

const TABS = ["fulfillments", "orders", "users", "moderation"];

const FULFILLMENT_STATUSES = ["PENDING", "PROCESSING", "COMPLETED", "FAILED", "CANCELLED"];
const ORDER_STATUSES = ["PENDING", "PAID", "PROCESSING", "DELIVERED", "CANCELLED", "REFUNDED"];

/**
 * One row of the top-up queue. Fulfilment is manual on purpose: no provider is
 * wired up, so an operator marks the delivery and records the reference.
 */
function FulfillmentRow({ item, t, onUpdated }) {
    const [status, setStatus] = useState(item.status);
    const [reference, setReference] = useState(item.providerReference || "");
    const [reason, setReason] = useState(item.failureReason || "");
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState("");

    async function save() {
        setBusy(true);
        setError("");

        try {
            onUpdated(await updateFulfillmentStatus(item.id, {
                status,
                providerReference: reference.trim() || null,
                failureReason: status === "FAILED" ? reason.trim() || null : null,
            }));
        } catch (requestError) {
            setError(getApiErrorMessage(requestError));
        } finally {
            setBusy(false);
        }
    }

    return (
        <li className="admin-row">
            <div className="admin-row__main">
                <strong>{item.productName}</strong>
                <small>
                    {item.orderNumber} · {item.playerAccountId || t("admin.noPlayerId")}
                </small>
                <small>
                    {item.inGameAmount} {item.inGameCurrencyName}
                    {item.bonusAmount > 0 && ` (+${item.bonusAmount})`} · {formatDateTime(item.createdAt)}
                </small>
            </div>

            <div className="admin-row__controls">
                <select value={status} onChange={(event) => setStatus(event.target.value)}>
                    {FULFILLMENT_STATUSES.map((value) => (
                        <option key={value} value={value}>{formatEnum(value)}</option>
                    ))}
                </select>

                <input
                    placeholder={t("admin.reference")}
                    value={reference}
                    onChange={(event) => setReference(event.target.value)}
                />

                {status === "FAILED" && (
                    <input
                        placeholder={t("admin.failureReason")}
                        value={reason}
                        onChange={(event) => setReason(event.target.value)}
                    />
                )}

                <button disabled={busy} type="button" onClick={save}>
                    {busy ? t("account.working") : t("admin.apply")}
                </button>
            </div>

            {error && <p className="admin-row__error">{error}</p>}
        </li>
    );
}

function FulfillmentsPanel({ t }) {
    const [status, setStatus] = useState("");
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const load = useCallback(async (signal) => {
        setLoading(true);
        setError("");

        try {
            const page = await getTopUpFulfillments(status, 0, 50, signal);
            if (signal?.aborted) return;
            setItems(page?.content || []);
        } catch (requestError) {
            if (!signal?.aborted) setError(getApiErrorMessage(requestError));
        } finally {
            if (!signal?.aborted) setLoading(false);
        }
    }, [status]);

    useEffect(() => {
        const controller = new AbortController();
        // Deferred: load() flips the loading flag before its first await, and
        // doing that inside the effect body cascades an extra render.
        const pending = Promise.resolve().then(() => load(controller.signal));

        return () => {
            controller.abort();
            pending.catch(() => {});
        };
    }, [load]);

    return (
        <>
            <header className="admin-panel__header">
                <div>
                    <h2>{t("admin.fulfillments")}</h2>
                    <p>{t("admin.fulfillmentsText")}</p>
                </div>

                <select value={status} onChange={(event) => setStatus(event.target.value)}>
                    <option value="">{t("admin.allStatuses")}</option>
                    {FULFILLMENT_STATUSES.map((value) => (
                        <option key={value} value={value}>{formatEnum(value)}</option>
                    ))}
                </select>
            </header>

            {loading && <div className="admin-empty">{t("common.loading")}</div>}
            {!loading && error && <div className="admin-empty is-error">{error}</div>}
            {!loading && !error && items.length === 0 && (
                <div className="admin-empty">{t("admin.noFulfillments")}</div>
            )}

            {!loading && !error && items.length > 0 && (
                <ul className="admin-rows">
                    {items.map((item) => (
                        <FulfillmentRow
                            item={item}
                            key={item.id}
                            t={t}
                            onUpdated={(updated) => setItems((current) => current.map(
                                (row) => (row.id === updated.id ? updated : row),
                            ))}
                        />
                    ))}
                </ul>
            )}
        </>
    );
}

function OrdersPanel({ t }) {
    const [userId, setUserId] = useState("");
    const [orders, setOrders] = useState(null);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState("");

    async function search(event) {
        event.preventDefault();
        setBusy(true);
        setError("");

        try {
            setOrders(await getOrdersByUser(Number(userId)));
        } catch (requestError) {
            setOrders(null);
            setError(getApiErrorMessage(requestError));
        } finally {
            setBusy(false);
        }
    }

    async function changeStatus(orderId, status) {
        setError("");

        try {
            const updated = await updateOrderStatus(orderId, status);
            setOrders((current) => current.map((order) => (order.id === orderId ? updated : order)));
        } catch (requestError) {
            setError(getApiErrorMessage(requestError));
        }
    }

    return (
        <>
            <header className="admin-panel__header">
                <div>
                    <h2>{t("admin.orders")}</h2>
                    <p>{t("admin.ordersText")}</p>
                </div>
            </header>

            <form className="admin-search" onSubmit={search}>
                <input
                    min="1"
                    placeholder={t("admin.userId")}
                    required
                    type="number"
                    value={userId}
                    onChange={(event) => setUserId(event.target.value)}
                />
                <button disabled={busy} type="submit">
                    <Search size={16} />
                    {busy ? t("account.working") : t("admin.search")}
                </button>
            </form>

            {error && <div className="admin-empty is-error">{error}</div>}

            {orders !== null && orders.length === 0 && (
                <div className="admin-empty">{t("admin.noOrders")}</div>
            )}

            {orders !== null && orders.length > 0 && (
                <ul className="admin-rows">
                    {orders.map((order) => (
                        <li className="admin-row" key={order.id}>
                            <div className="admin-row__main">
                                <strong>{order.orderNumber}</strong>
                                <small>{formatDateTime(order.createdAt)}</small>
                                <small>{formatMoney(order.totalAmount, null)}</small>
                            </div>

                            <div className="admin-row__controls">
                                <select
                                    value={order.status}
                                    onChange={(event) => changeStatus(order.id, event.target.value)}
                                >
                                    {ORDER_STATUSES.map((value) => (
                                        <option key={value} value={value}>{formatEnum(value)}</option>
                                    ))}
                                </select>
                            </div>
                        </li>
                    ))}
                </ul>
            )}
        </>
    );
}

function UsersPanel({ t }) {
    const [term, setTerm] = useState("");
    const [found, setFound] = useState(null);
    const [amount, setAmount] = useState("");
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState("");
    const [notice, setNotice] = useState("");

    async function search(event) {
        event.preventDefault();
        setBusy(true);
        setError("");
        setNotice("");

        try {
            // A digits-only term is an id; anything else is a username.
            const value = term.trim();
            setFound(/^\d+$/.test(value)
                ? await findUserById(Number(value))
                : await findUserByUsername(value));
        } catch (requestError) {
            setFound(null);
            setError(getApiErrorMessage(requestError));
        } finally {
            setBusy(false);
        }
    }

    async function applyBalance() {
        setBusy(true);
        setError("");
        setNotice("");

        try {
            const updated = await updateBalance(found.id, Number(amount));
            setFound(updated);
            setAmount("");
            setNotice(t("admin.balanceUpdated"));
        } catch (requestError) {
            setError(getApiErrorMessage(requestError));
        } finally {
            setBusy(false);
        }
    }

    return (
        <>
            <header className="admin-panel__header">
                <div>
                    <h2>{t("admin.users")}</h2>
                    <p>{t("admin.usersText")}</p>
                </div>
            </header>

            <form className="admin-search" onSubmit={search}>
                <input
                    placeholder={t("admin.userSearchPlaceholder")}
                    required
                    value={term}
                    onChange={(event) => setTerm(event.target.value)}
                />
                <button disabled={busy} type="submit">
                    <Search size={16} />
                    {busy ? t("account.working") : t("admin.search")}
                </button>
            </form>

            {error && <div className="admin-empty is-error">{error}</div>}
            {notice && <div className="admin-empty is-success">{notice}</div>}

            {found && (
                <div className="admin-user">
                    <div className="admin-user__identity">
                        <strong>{found.username}</strong>
                        <small>{found.email}</small>
                        <span className="admin-tag">#{found.id}</span>
                        {(found.seller ?? found.isSeller) && (
                            <span className="admin-tag is-accent">
                                <BadgeCheck size={13} /> {t("account.seller")}
                            </span>
                        )}
                        {(found.roles || []).map((role) => (
                            <span className="admin-tag" key={role}>{role.replace("ROLE_", "")}</span>
                        ))}
                    </div>

                    <div className="admin-user__balance">
                        <span>
                            <Wallet size={15} /> {t("account.balance")}
                        </span>
                        <strong>{formatMoney(found.balance ?? 0, null)}</strong>
                    </div>

                    <div className="admin-user__actions">
                        <input
                            placeholder={t("admin.amountPlaceholder")}
                            step="0.01"
                            type="number"
                            value={amount}
                            onChange={(event) => setAmount(event.target.value)}
                        />
                        <button disabled={busy || !amount} type="button" onClick={applyBalance}>
                            {t("admin.applyBalance")}
                        </button>
                    </div>
                    <p className="admin-user__hint">{t("admin.balanceHint")}</p>
                </div>
            )}
        </>
    );
}

/**
 * The remaining admin endpoints act on a single id and answer with the updated
 * record, so one small id-and-go form each is the honest shape for them.
 */
function ModerationPanel({ t }) {
    const actions = [
        { key: "review", label: t("admin.approveReview"), run: approveReview },
        { key: "seller", label: t("admin.approveSeller"), run: approveSeller },
        { key: "product", label: t("admin.deactivateProduct"), run: deactivateProduct },
        { key: "tournament", label: t("admin.cancelTournament"), run: cancelTournament },
    ];

    const [values, setValues] = useState({});
    const [busyKey, setBusyKey] = useState("");
    const [results, setResults] = useState({});

    async function run(action) {
        const id = Number(values[action.key]);
        if (!id) return;

        setBusyKey(action.key);
        setResults((current) => ({ ...current, [action.key]: null }));

        try {
            await action.run(id);
            setResults((current) => ({
                ...current,
                [action.key]: { ok: true, message: t("admin.done") },
            }));
            setValues((current) => ({ ...current, [action.key]: "" }));
        } catch (requestError) {
            setResults((current) => ({
                ...current,
                [action.key]: { ok: false, message: getApiErrorMessage(requestError) },
            }));
        } finally {
            setBusyKey("");
        }
    }

    return (
        <>
            <header className="admin-panel__header">
                <div>
                    <h2>{t("admin.moderation")}</h2>
                    <p>{t("admin.moderationText")}</p>
                </div>
            </header>

            <ul className="admin-actions">
                {actions.map((action) => {
                    const result = results[action.key];

                    return (
                        <li key={action.key}>
                            <span>{action.label}</span>

                            <div>
                                <input
                                    min="1"
                                    placeholder="ID"
                                    type="number"
                                    value={values[action.key] || ""}
                                    onChange={(event) => setValues((current) => ({
                                        ...current,
                                        [action.key]: event.target.value,
                                    }))}
                                />
                                <button
                                    disabled={busyKey === action.key || !values[action.key]}
                                    type="button"
                                    onClick={() => run(action)}
                                >
                                    {busyKey === action.key ? t("account.working") : t("admin.run")}
                                </button>
                            </div>

                            {result && (
                                <small className={result.ok ? "is-success" : "is-error"}>
                                    {result.message}
                                </small>
                            )}
                        </li>
                    );
                })}
            </ul>
        </>
    );
}

export default function AdminPage() {
    const { t } = useTranslation();
    const { isAdmin, isAuthenticated, profile } = useAccount();
    const [tab, setTab] = useState("fulfillments");

    if (!isAuthenticated) {
        return (
            <div className="container admin-page">
                <div className="admin-guard">
                    <ShieldAlert size={30} />
                    <h1>{t("admin.guardTitle")}</h1>
                    <p>{t("account.guardText")}</p>
                </div>
            </div>
        );
    }

    // The profile arrives after mount; treating "not loaded yet" as "not admin"
    // would flash a refusal at an account that is in fact allowed here.
    if (!profile) {
        return (
            <div className="container admin-page">
                <div className="admin-guard">{t("common.loading")}</div>
            </div>
        );
    }

    if (!isAdmin) {
        return (
            <div className="container admin-page">
                <div className="admin-guard">
                    <ShieldAlert size={30} />
                    <h1>{t("admin.deniedTitle")}</h1>
                    <p>{t("admin.deniedText")}</p>
                </div>
            </div>
        );
    }

    return (
        <div className="container admin-page">
            <header className="admin-page__header">
                <p className="section-kicker">{t("admin.eyebrow")}</p>
                <h1>{t("admin.title")}</h1>
                <p>{t("admin.lead")}</p>
            </header>

            <nav className="admin-tabs" aria-label={t("admin.title")}>
                {TABS.map((value) => (
                    <button
                        className={tab === value ? "is-active" : ""}
                        key={value}
                        type="button"
                        onClick={() => setTab(value)}
                    >
                        {value === "fulfillments" && <PackageSearch size={16} />}
                        {value === "orders" && <Receipt size={16} />}
                        {value === "users" && <Wallet size={16} />}
                        {value === "moderation" && <BadgeCheck size={16} />}
                        {t(`admin.${value}`)}
                    </button>
                ))}
            </nav>

            <section className="admin-panel">
                {tab === "fulfillments" && <FulfillmentsPanel t={t} />}
                {tab === "orders" && <OrdersPanel t={t} />}
                {tab === "users" && <UsersPanel t={t} />}
                {tab === "moderation" && <ModerationPanel t={t} />}
            </section>
        </div>
    );
}
