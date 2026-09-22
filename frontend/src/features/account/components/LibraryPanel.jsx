import { useEffect, useState } from "react";
import { Copy, ExternalLink, Eye, EyeOff, Library } from "lucide-react";

import { getMyLibrary } from "../../../api/libraryApi.js";
import { getApiErrorMessage } from "../../../api/httpClient.js";
import { formatDateTime, formatEnum, formatPlatform } from "../../../utils/formatters.js";

/**
 * A key is the thing of value in an order, so it stays hidden until asked for
 * — screen shares and shoulders are the realistic threat here, not attackers.
 */
function CodeReveal({ code, t }) {
    const [shown, setShown] = useState(false);
    const [copied, setCopied] = useState(false);

    async function copy() {
        try {
            await navigator.clipboard.writeText(code);
            setCopied(true);
            window.setTimeout(() => setCopied(false), 2000);
        } catch {
            // Clipboard access can be refused; revealing the code is enough.
            setShown(true);
        }
    }

    return (
        <div className="library-code">
            <code>{shown ? code : "•".repeat(Math.min(code.length, 20))}</code>

            <button type="button" onClick={() => setShown((current) => !current)}>
                {shown ? <EyeOff size={15} /> : <Eye size={15} />}
                {shown ? t("library.hide") : t("library.reveal")}
            </button>

            <button type="button" onClick={copy}>
                <Copy size={15} />
                {copied ? t("library.copied") : t("library.copy")}
            </button>
        </div>
    );
}

export default function LibraryPanel({ t }) {
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const controller = new AbortController();

        getMyLibrary(controller.signal)
            .then((data) => {
                if (!controller.signal.aborted) setItems(data || []);
            })
            .catch((requestError) => {
                if (!controller.signal.aborted) setError(getApiErrorMessage(requestError));
            })
            .finally(() => {
                if (!controller.signal.aborted) setLoading(false);
            });

        return () => controller.abort();
    }, []);

    return (
        <>
            <header className="account-panel__header">
                <div>
                    <h2>{t("library.title")}</h2>
                    <p>{t("library.text")}</p>
                </div>
                <span>{items.length}</span>
            </header>

            {loading && <div className="account-empty">{t("common.loading")}</div>}
            {!loading && error && <div className="account-empty is-error">{error}</div>}

            {!loading && !error && items.length === 0 && (
                <div className="account-empty">
                    <Library size={26} />
                    <p>{t("library.empty")}</p>
                </div>
            )}

            {!loading && !error && items.length > 0 && (
                <ul className="library-list">
                    {items.map((item) => (
                        <li key={item.id}>
                            <div className="library-item__head">
                                <span className="account-rows__main">
                                    <strong>{item.productName}</strong>
                                    <small>
                                        {formatPlatform(item.platform)} · {formatEnum(item.productType)}
                                        {" · "}
                                        {formatDateTime(item.grantedAt)}
                                    </small>
                                </span>
                                <span className="account-tag">{formatEnum(item.status)}</span>
                            </div>

                            {item.digitalCode && <CodeReveal code={item.digitalCode} t={t} />}

                            {item.redemptionInstructions && (
                                <p className="library-item__note">{item.redemptionInstructions}</p>
                            )}

                            {item.redemptionUrl && (
                                <a
                                    className="library-item__link"
                                    href={item.redemptionUrl}
                                    rel="noreferrer noopener"
                                    target="_blank"
                                >
                                    {t("library.redeem")}
                                    <ExternalLink size={14} />
                                </a>
                            )}
                        </li>
                    ))}
                </ul>
            )}
        </>
    );
}
