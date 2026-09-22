import { useEffect, useState } from "react";
import { BadgeCheck, Clock, Store } from "lucide-react";

import { createSellerProfile, getSellerProfile } from "../../../api/sellerApi.js";
import { getApiErrorMessage } from "../../../api/httpClient.js";
import { formatDateTime } from "../../../utils/formatters.js";

const emptyForm = { shopName: "", bio: "", contactEmail: "", phoneNumber: "" };

export default function SellerPanel({ t, email }) {
    const [profile, setProfile] = useState(null);
    const [form, setForm] = useState({ ...emptyForm, contactEmail: email || "" });
    const [loading, setLoading] = useState(true);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        const controller = new AbortController();

        getSellerProfile(controller.signal)
            .then((data) => {
                if (!controller.signal.aborted) setProfile(data);
            })
            .catch((requestError) => {
                if (!controller.signal.aborted) setError(getApiErrorMessage(requestError));
            })
            .finally(() => {
                if (!controller.signal.aborted) setLoading(false);
            });

        return () => controller.abort();
    }, []);

    function update(field) {
        return (event) => setForm((current) => ({ ...current, [field]: event.target.value }));
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setBusy(true);
        setError("");

        try {
            setProfile(await createSellerProfile({
                shopName: form.shopName.trim(),
                bio: form.bio.trim() || null,
                contactEmail: form.contactEmail.trim(),
                phoneNumber: form.phoneNumber.trim() || null,
            }));
        } catch (requestError) {
            setError(getApiErrorMessage(requestError));
        } finally {
            setBusy(false);
        }
    }

    if (loading) return <div className="account-empty">{t("common.loading")}</div>;

    if (profile) {
        return (
            <>
                <header className="account-panel__header">
                    <div>
                        <h2>{t("seller.title")}</h2>
                        <p>{t("seller.text")}</p>
                    </div>
                    <span className={`seller-state ${profile.approved ?? profile.isApproved ? "is-approved" : ""}`}>
                        {profile.approved ?? profile.isApproved
                            ? <><BadgeCheck size={15} /> {t("seller.approved")}</>
                            : <><Clock size={15} /> {t("seller.pending")}</>}
                    </span>
                </header>

                <dl className="seller-facts">
                    <div>
                        <dt>{t("seller.shopName")}</dt>
                        <dd>{profile.shopName}</dd>
                    </div>
                    <div>
                        <dt>{t("seller.contactEmail")}</dt>
                        <dd>{profile.contactEmail}</dd>
                    </div>
                    <div>
                        <dt>{t("account.phone")}</dt>
                        <dd>{profile.phoneNumber || "—"}</dd>
                    </div>
                    <div>
                        <dt>{t("seller.since")}</dt>
                        <dd>{formatDateTime(profile.createdAt)}</dd>
                    </div>
                    {profile.bio && (
                        <div className="is-wide">
                            <dt>{t("seller.bio")}</dt>
                            <dd>{profile.bio}</dd>
                        </div>
                    )}
                </dl>

                {!(profile.approved ?? profile.isApproved) && (
                    <p className="account-form__note">{t("seller.pendingNote")}</p>
                )}
            </>
        );
    }

    return (
        <form className="account-form" onSubmit={handleSubmit}>
            <header className="account-panel__header">
                <div>
                    <h2>{t("seller.applyTitle")}</h2>
                    <p>{t("seller.applyText")}</p>
                </div>
                <Store size={22} />
            </header>

            <div className="account-form__grid">
                <label>
                    {t("seller.shopName")}
                    <input required value={form.shopName} onChange={update("shopName")} />
                </label>
                <label>
                    {t("seller.contactEmail")}
                    <input required type="email" value={form.contactEmail} onChange={update("contactEmail")} />
                </label>
                <label>
                    {t("account.phone")}
                    <input value={form.phoneNumber} onChange={update("phoneNumber")} />
                </label>
                <label className="account-form__wide">
                    {t("seller.bio")}
                    <input value={form.bio} onChange={update("bio")} />
                </label>
            </div>

            {error && <p className="account-form__note is-error">{error}</p>}

            <footer className="account-form__actions">
                <button disabled={busy} type="submit">
                    {busy ? t("account.working") : t("seller.apply")}
                </button>
            </footer>
        </form>
    );
}
