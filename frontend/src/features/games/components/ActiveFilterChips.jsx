import { X } from "lucide-react";

import { formatEnum } from "../../../utils/formatters.js";
import { useTranslation } from "../../../i18n/index.jsx";

function getActiveFilters(filters, t) {
    const items = [];

    if (filters.query) {
        items.push({
            key: "q",
            label: t("games.searchPrefix", { value: filters.query }),
        });
    }

    if (filters.accessType) {
        items.push({
            key: "accessType",
            label: formatEnum(filters.accessType),
        });
    }

    if (filters.genres) {
        items.push({
            key: "genres",
            label: formatEnum(filters.genres),
        });
    }

    if (filters.platform) {
        items.push({
            key: "platform",
            label: formatEnum(filters.platform),
        });
    }

    if (filters.releaseYear) {
        items.push({
            key: "releaseYear",
            label: filters.releaseYear,
        });
    }

    if (filters.publisher) {
        items.push({
            key: "publisher",
            label: filters.publisher,
        });
    }

    if (filters.minPrice) {
        items.push({
            key: "minPrice",
            label: t("games.fromPrice", { value: filters.minPrice }),
        });
    }

    if (filters.maxPrice) {
        items.push({
            key: "maxPrice",
            label: t("games.upToPrice", { value: filters.maxPrice }),
        });
    }

    return items;
}

export default function ActiveFilterChips({
                                              filters,
                                              onRemove,
                                              onClearAll,
}) {
    const { t } = useTranslation();
    const activeFilters = getActiveFilters(filters, t);

    if (activeFilters.length === 0) {
        return null;
    }

    return (
        <section className="active-filters" aria-label={t("games.activeFilters")}>
            <div className="active-filters__chips">
                {activeFilters.map((filter) => (
                    <button
                        className="filter-chip"
                        key={filter.key}
                        type="button"
                        onClick={() => onRemove(filter.key)}
                    >
                        {filter.label}
                        <X size={14} />
                    </button>
                ))}
            </div>

            <button
                className="clear-all-button"
                type="button"
                onClick={onClearAll}
            >
                {t("common.clearAll")}
            </button>
        </section>
    );
}
