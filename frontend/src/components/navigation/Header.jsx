import {
    Gamepad2,
    Globe2,
    Heart,
    ShoppingBag,
    Search,
    ShoppingCart,
    Sparkles,
    Trophy,
    UserRound,
} from "lucide-react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { useState } from "react";
import { useTranslation } from "../../i18n/index.jsx";

export default function Header() {
    const navigate = useNavigate();
    const [query, setQuery] = useState("");
    const { language, setLanguage, t } = useTranslation();

    function handleSearch(event) {
        event.preventDefault();

        const normalizedQuery = query.trim();

        navigate(
            normalizedQuery
                ? `/games?q=${encodeURIComponent(normalizedQuery)}`
                : "/games",
        );
    }

    function handleLanguageChange(event) {
        const nextLanguage = event.target.value;
        setLanguage(nextLanguage);
    }

    return (
        <header className="site-header">
            <div className="container header-inner">
                <Link className="brand" to="/" aria-label={t("nav.home")}>
                    Game<span>Sphere</span>
                </Link>

                <nav className="primary-nav" aria-label={t("nav.home")}>
                    <NavLink to="/games">
                        <Gamepad2 size={16} />
                        {t("nav.games")}
                    </NavLink>
                    <NavLink to="/top-ups">
                        <Sparkles size={16} />
                        {t("nav.topUps")}
                    </NavLink>
                    <NavLink to="/marketplace">
                        <ShoppingBag size={16} />
                        {t("nav.marketplace")}
                    </NavLink>
                    <NavLink to="/tournaments">
                        <Trophy size={16} />
                        {t("nav.tournaments")}
                    </NavLink>
                </nav>

                <form className="header-search" onSubmit={handleSearch}>
                    <input
                        type="search"
                        value={query}
                        placeholder={t("nav.search")}
                        aria-label={t("nav.search")}
                        onChange={(event) => setQuery(event.target.value)}
                    />

                    <button type="submit" title={t("nav.search")}>
                        <Search size={18} />
                    </button>
                </form>

                <div className="header-actions">
                    <label className="language-control" title={t("nav.language")}>
                        <Globe2 size={17} />
                        <select
                            aria-label={t("nav.language")}
                            value={language}
                            onChange={handleLanguageChange}
                        >
                            <option value="AZ">AZ</option>
                            <option value="TR">TR</option>
                            <option value="EN">EN</option>
                            <option value="RU">RU</option>
                        </select>
                    </label>

                    <button className="icon-button" type="button" title={t("nav.wishlist")}>
                        <Heart size={19} />
                    </button>

                    <button className="icon-button" type="button" title={t("nav.cart")}>
                        <ShoppingCart size={19} />
                    </button>

                    <button className="icon-button" type="button" title={t("nav.profile")}>
                        <UserRound size={19} />
                    </button>
                </div>
            </div>
        </header>
    );
}
