import { useEffect, useState } from "react";
import { Gift, Inbox, Send } from "lucide-react";

import { claimGift, getReceivedGifts, getSentGifts } from "../../../api/giftApi.js";
import { getApiErrorMessage } from "../../../api/httpClient.js";
import { formatDateTime, formatEnum } from "../../../utils/formatters.js";

function GiftRow({ gift, side, t, onClaimed }) {
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState("");
    const claimable = side === "received" && gift.status === "PENDING" && gift.claimToken;

    async function claim() {
        setBusy(true);
        setError("");

        try {
            onClaimed(await claimGift(gift.claimToken));
        } catch (requestError) {
            setError(getApiErrorMessage(requestError));
        } finally {
            setBusy(false);
        }
    }

    return (
        <li>
            <span className="account-rows__main">
                <strong>{gift.productName}</strong>
                <small>
                    {side === "sent"
                        ? t("gifts.toRecipient", { email: gift.recipientEmail })
                        : t("gifts.fromSender", { sender: gift.senderUsername })}
                    {gift.expiresAt && ` · ${t("gifts.expires", { date: formatDateTime(gift.expiresAt) })}`}
                </small>
                {gift.message && <small className="gift-message">“{gift.message}”</small>}
                {error && <small className="gift-error">{error}</small>}
            </span>

            <span className="account-tag">{formatEnum(gift.status)}</span>

            {claimable && (
                <button disabled={busy} type="button" onClick={claim}>
                    {busy ? t("account.working") : t("gifts.claim")}
                </button>
            )}
        </li>
    );
}

export default function GiftsPanel({ t }) {
    const [side, setSide] = useState("received");
    const [received, setReceived] = useState([]);
    const [sent, setSent] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const controller = new AbortController();

        Promise.allSettled([
            getReceivedGifts(controller.signal),
            getSentGifts(controller.signal),
        ])
            .then(([receivedResult, sentResult]) => {
                if (controller.signal.aborted) return;

                if (receivedResult.status === "fulfilled") setReceived(receivedResult.value || []);
                if (sentResult.status === "fulfilled") setSent(sentResult.value || []);

                const failed = [receivedResult, sentResult].find((result) => result.status === "rejected");
                if (failed) setError(getApiErrorMessage(failed.reason));
            })
            .finally(() => {
                if (!controller.signal.aborted) setLoading(false);
            });

        return () => controller.abort();
    }, []);

    const rows = side === "received" ? received : sent;

    function replaceClaimed(updated) {
        setReceived((current) => current.map((gift) => (gift.id === updated.id ? updated : gift)));
    }

    return (
        <>
            <header className="account-panel__header">
                <div>
                    <h2>{t("gifts.title")}</h2>
                    <p>{t("gifts.text")}</p>
                </div>
                <span>{rows.length}</span>
            </header>

            <div className="gift-switch">
                <button
                    className={side === "received" ? "is-active" : ""}
                    type="button"
                    onClick={() => setSide("received")}
                >
                    <Inbox size={15} />
                    {t("gifts.received")} ({received.length})
                </button>
                <button
                    className={side === "sent" ? "is-active" : ""}
                    type="button"
                    onClick={() => setSide("sent")}
                >
                    <Send size={15} />
                    {t("gifts.sent")} ({sent.length})
                </button>
            </div>

            {loading && <div className="account-empty">{t("common.loading")}</div>}
            {!loading && error && <div className="account-empty is-error">{error}</div>}

            {!loading && !error && rows.length === 0 && (
                <div className="account-empty">
                    <Gift size={26} />
                    <p>{side === "received" ? t("gifts.noReceived") : t("gifts.noSent")}</p>
                </div>
            )}

            {!loading && !error && rows.length > 0 && (
                <ul className="account-rows">
                    {rows.map((gift) => (
                        <GiftRow gift={gift} key={gift.id} side={side} t={t} onClaimed={replaceClaimed} />
                    ))}
                </ul>
            )}
        </>
    );
}
