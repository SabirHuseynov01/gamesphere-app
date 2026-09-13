import { SearchX } from "lucide-react";
import { useTranslation } from "../../../i18n/index.jsx";

export default function EmptyCatalogState({ onReset }) {
    const { t } = useTranslation();
    return (
        <section className="empty-catalog-state">
            <div className="empty-catalog-state__icon">
                <SearchX size={30} />
            </div>

            <h2>{t("games.noResults")}</h2>
            <p>{t("games.tryFilters")}</p>

            <button type="button" onClick={onReset}>
                {t("games.resetFilters")}
            </button>
        </section>
    );
}
