const platformOptions = [
    { value: "", key: "allPlatforms" }, { value: "PC", key: "pc" },
    { value: "PLAYSTATION", key: "playstation" }, { value: "XBOX", key: "xbox" },
    { value: "NINTENDO", key: "nintendo" }, { value: "MOBILE", key: "mobile" },
];

const productTypeOptions = [
    { value: "", key: "allProductTypes" }, { value: "GAME", key: "game" },
    { value: "DLC", key: "dlc" }, { value: "IN_GAME_CURRENCY", key: "currency" },
    { value: "IN_GAME_ITEM", key: "item" }, { value: "BATTLE_PASS", key: "battlePass" },
    { value: "SUBSCRIPTION", key: "subscription" }, { value: "BUNDLE", key: "bundle" },
];

export default function OfferFilters({
                                         filters,
                                         onChange,
                                         onReset,
}) {
    const { t } = useTranslation();
    function updateFilter(key, value) {
        onChange({
            ...filters,
            [key]: value,
        });
    }

    return (
        <section className="offer-filters" aria-label={t("offers.filters")}>
            <label>
                {t("games.platform")}
                <select
                    value={filters.platform}
                    onChange={(event) => updateFilter("platform", event.target.value)}
                >
                    {platformOptions.map((option) => (
                        <option key={option.value} value={option.value}>
                            {option.key === "allPlatforms" ? t("games.allPlatforms") : option.value}
                        </option>
                    ))}
                </select>
            </label>

            <label>
                {t("offers.productType")}
                <select
                    value={filters.productType}
                    onChange={(event) =>
                        updateFilter("productType", event.target.value)
                    }
                >
                    {productTypeOptions.map((option) => (
                        <option key={option.value} value={option.value}>
                            {t(`offers.${option.key}`)}
                        </option>
                    ))}
                </select>
            </label>

            <label>
                {t("offers.storeType")}
                <select
                    value={filters.officialStore}
                    onChange={(event) =>
                        updateFilter("officialStore", event.target.value)
                    }
                >
                    <option value="">{t("offers.allStores")}</option>
                    <option value="true">{t("offers.officialStores")}</option>
                    <option value="false">{t("offers.partnerStores")}</option>
                </select>
            </label>

            <label>
                {t("offers.availability")}
                <select
                    value={filters.inStock}
                    onChange={(event) => updateFilter("inStock", event.target.value)}
                >
                    <option value="">{t("offers.allOffers")}</option>
                    <option value="true">{t("offers.inStock")}</option>
                    <option value="false">{t("offers.outOfStock")}</option>
                </select>
            </label>

            <label>
                {t("offers.sort")}
                <select
                    value={filters.sort}
                    onChange={(event) => updateFilter("sort", event.target.value)}
                >
                    <option value="PRICE_ASC">{t("offers.lowest")}</option>
                    <option value="PRICE_DESC">{t("offers.highest")}</option>
                    <option value="NEWEST">{t("offers.recentlyChecked")}</option>
                </select>
            </label>

            <button className="filter-reset" type="button" onClick={onReset}>
                {t("common.reset")}
            </button>
        </section>
    );
}
import { useTranslation } from "../../../i18n/index.jsx";
