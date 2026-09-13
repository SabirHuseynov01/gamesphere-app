import { Filter } from "lucide-react";
import { useState } from "react";
import { useTranslation } from "../../../i18n/index.jsx";

const platformOptions = [
    { value: "", key: "allPlatforms" },
    { value: "PC", label: "PC" },
    { value: "PLAYSTATION", key: "playstation" },
    { value: "XBOX", key: "xbox" },
    { value: "NINTENDO", key: "nintendo" },
    { value: "MOBILE", key: "mobile" },
];

const genreOptions = [
    { value: "", key: "allGenres" },
    { value: "ACTION", key: "action" },
    { value: "ADVENTURE", key: "adventure" },
    { value: "FPS", key: "fps" },
    { value: "RPG", key: "rpg" },
    { value: "RACING", key: "racing" },
    { value: "SPORTS", key: "sports" },
    { value: "STRATEGY", key: "strategy" },
    { value: "HORROR", key: "horror" },
    { value: "SURVIVAL", key: "survival" },
    { value: "MMO", key: "mmo" },
    { value: "SIMULATION", key: "simulation" },
    { value: "FIGHTING", key: "fighting" },
];

const currentYear = new Date().getFullYear();

const releaseYears = Array.from(
    { length: currentYear - 2014 },
    (_, index) => currentYear - index,
);

export default function BrowseFilters({
                                          filters,
                                          onApply,
                                          onReset,
}) {
    const { t } = useTranslation();
    const [draft, setDraft] = useState(filters);

    function updateDraft(key, value) {
        setDraft((current) => ({
            ...current,
            [key]: value,
        }));
    }

    function handleSubmit(event) {
        event.preventDefault();
        onApply(draft);
    }

    return (
        <aside className="browse-filters">
            <div className="browse-filters__heading">
                <Filter size={19} />
                <h2>{t("games.filters")}</h2>
            </div>

            <form onSubmit={handleSubmit}>
                <label className="filter-field">
                    <span>{t("games.gameType")}</span>
                    <select
                        value={draft.accessType}
                        onChange={(event) =>
                            updateDraft("accessType", event.target.value)
                        }
                    >
                        <option value="">{t("games.allGames")}</option>
                        <option value="FREE_TO_PLAY">{t("games.freeToPlay")}</option>
                        <option value="PAID">{t("games.paid")}</option>
                    </select>
                </label>

                <label className="filter-field">
                    <span>{t("games.genre")}</span>
                    <select
                        value={draft.genres}
                        onChange={(event) => updateDraft("genres", event.target.value)}
                    >
                        {genreOptions.map((option) => (
                            <option key={option.value} value={option.value}>
                                {option.key === "allGenres" ? t("games.allGenres") : option.value}
                            </option>
                        ))}
                    </select>
                </label>

                <label className="filter-field">
                    <span>{t("games.platform")}</span>
                    <select
                        value={draft.platform}
                        onChange={(event) => updateDraft("platform", event.target.value)}
                    >
                        {platformOptions.map((option) => (
                            <option key={option.value} value={option.value}>
                                {option.key === "allPlatforms" ? t("games.allPlatforms") : option.value}
                            </option>
                        ))}
                    </select>
                </label>

                <label className="filter-field">
                    <span>{t("games.releaseYear")}</span>
                    <select
                        value={draft.releaseYear}
                        onChange={(event) =>
                            updateDraft("releaseYear", event.target.value)
                        }
                    >
                        <option value="">{t("games.allYears")}</option>

                        {releaseYears.map((year) => (
                            <option key={year} value={year}>
                                {year}
                            </option>
                        ))}
                    </select>
                </label>

                <label className="filter-field">
                    <span>{t("games.publisher")}</span>
                    <input
                        type="search"
                        value={draft.publisher}
                        placeholder={t("games.publisherPlaceholder")}
                        onChange={(event) =>
                            updateDraft("publisher", event.target.value)
                        }
                    />
                </label>

                <div className="price-range">
                    <label className="filter-field">
                        <span>{t("games.minPrice")}</span>
                        <input
                            type="number"
                            min="0"
                            step="0.01"
                            value={draft.minPrice}
                            placeholder="0"
                            onChange={(event) =>
                                updateDraft("minPrice", event.target.value)
                            }
                        />
                    </label>

                    <label className="filter-field">
                        <span>{t("games.maxPrice")}</span>
                        <input
                            type="number"
                            min="0"
                            step="0.01"
                            value={draft.maxPrice}
                            placeholder="100"
                            onChange={(event) =>
                                updateDraft("maxPrice", event.target.value)
                            }
                        />
                    </label>
                </div>

                <label className="filter-field">
                    <span>{t("games.sortBy")}</span>
                    <select
                        value={draft.sortBy}
                        onChange={(event) => updateDraft("sortBy", event.target.value)}
                    >
                        <option value="ALPHABETICAL">{t("games.alphabetical")}</option>
                        <option value="NEWEST">{t("games.newest")}</option>
                        <option value="OLDEST">{t("games.oldest")}</option>
                        <option value="PRICE_LOW_TO_HIGH">{t("games.lowHigh")}</option>
                        <option value="PRICE_HIGH_TO_LOW">{t("games.highLow")}</option>
                    </select>
                </label>

                <div className="filter-actions">
                    <button className="filter-apply" type="submit">
                        {t("games.apply")}
                    </button>

                    <button
                        className="filter-reset"
                        type="button"
                        onClick={onReset}
                    >
                        {t("common.reset")}
                    </button>
                </div>
            </form>
        </aside>
    );
}
