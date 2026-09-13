import { Link } from "react-router-dom";
import { useTranslation } from "../i18n/index.jsx";

export default function NotFoundPage() {
    const { t } = useTranslation();
    return (
        <div className="container not-found">
            <span>404</span>
            <h1>{t("errors.notFound")}</h1>
            <Link to="/">{t("errors.returnGames")}</Link>
        </div>
    );
}
