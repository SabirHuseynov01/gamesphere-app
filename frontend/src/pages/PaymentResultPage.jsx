import { CheckCircle2, XCircle } from "lucide-react";
import { Link, useSearchParams } from "react-router-dom";

import { useTranslation } from "../i18n/index.jsx";
import "../styles/checkout.css";

/** Landing page Stripe returns to, for both the success and cancel URLs. */
export default function PaymentResultPage({ outcome }) {
    const { t } = useTranslation();
    const [params] = useSearchParams();
    const sessionId = params.get("session_id");
    const succeeded = outcome === "success";

    return (
        <div className="container checkout-page">
            <div className={`payment-result${succeeded ? " is-success" : " is-cancelled"}`}>
                {succeeded ? <CheckCircle2 size={44} /> : <XCircle size={44} />}

                <h1>{succeeded ? t("checkout.successTitle") : t("checkout.cancelTitle")}</h1>
                <p>{succeeded ? t("checkout.successText") : t("checkout.cancelText")}</p>

                {succeeded && sessionId && (
                    <code className="payment-result__session">{sessionId}</code>
                )}

                <div className="payment-result__actions">
                    <Link className="is-primary" to={succeeded ? "/account/orders" : "/checkout"}>
                        {succeeded ? t("account.orders") : t("checkout.tryAgain")}
                    </Link>
                    <Link to="/top-ups">{t("checkout.keepShopping")}</Link>
                </div>
            </div>
        </div>
    );
}
