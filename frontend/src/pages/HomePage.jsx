import { ArrowRight, CalendarDays, Gamepad2, Gift, Monitor, ShoppingBag, Tag, Trophy, Users, X } from "lucide-react";
import { Link } from "react-router-dom";
import { useEffect, useMemo, useState } from "react";

import { searchGames } from "../api/gameApi.js";
import { getCatalogProducts } from "../api/catalogApi.js";
import { getTopUpProducts } from "../api/topUpApi.js";
import MediaImage from "../components/MediaImage.jsx";
import HomeHeroCarousel from "../features/home/components/HomeHeroCarousel.jsx";
import { useTranslation } from "../i18n/index.jsx";
import { formatCurrency } from "../utils/formatters.js";
import { getGameArtwork } from "../utils/gameArtwork.js";
import { resolveMediaUrl } from "../utils/mediaUrl.js";
import "../styles/home.css";

const demoGames = [
    { id: "demo-cyberpunk", title: "Cyberpunk 2077", slug: "cyberpunk-2077", publisher: "CD PROJEKT RED", coverImageUrl: "/uploads/games/1/a738b9a7-3d6c-4be5-aa98-4574060cbe99.jpeg", genres: ["RPG", "Action"], releaseDate: "2020-12-10", lowestPrice: 29.99, currency: "USD" },
    { id: "demo-rdr2", title: "Red Dead Redemption 2", slug: "red-dead-redemption-2", publisher: "Rockstar Games", coverImageUrl: "/uploads/games/6/7dd3511b-f2b5-4870-b3a9-ad62c7a5f24e.jpeg", genres: ["Action", "Adventure"], releaseDate: "2018-10-26", lowestPrice: 19.99, currency: "USD" },
    { id: "demo-nfs", title: "Need for Speed Unbound", slug: "need-for-speed-unbound", publisher: "Criterion Games", coverImageUrl: "/uploads/games/3/12ee0127-52e2-4e64-b573-604d9c861c41.png", genres: ["Racing", "Action"], releaseDate: "2022-12-02", lowestPrice: 24.99, currency: "USD" },
    { id: "demo-mk", title: "Mortal Kombat 1", slug: "mortal-kombat-1", publisher: "NetherRealm Studios", coverImageUrl: "/uploads/games/4/55acfdff-6b91-4257-8844-af3cff02ebf5.jpeg", genres: ["Fighting", "Action"], releaseDate: "2023-09-19", lowestPrice: 39.99, currency: "USD" },
];

const platformItems = [["PC", "Steam, Epic and more", Monitor], ["PlayStation", "PS4 and PS5 stores", Gamepad2], ["Xbox", "Xbox and Microsoft Store", Gamepad2], ["Nintendo", "Switch editions", Gift]];
const demoTournaments = [{ title: "GameSphere Summer Cup", game: "Counter-Strike 2", date: "2026-08-22", players: "32 / 64", prize: "$500" }, { title: "Racing Rivals", game: "Forza Horizon 5", date: "2026-08-30", players: "18 / 32", prize: "$250" }, { title: "Fighter's Arena", game: "Mortal Kombat 1", date: "2026-09-05", players: "12 / 16", prize: "$150" }];

function genres(game) { const value = game?.genres ?? game?.genre ?? []; return Array.isArray(value) ? value : String(value).split(",").filter(Boolean); }

function HomeSectionHeading({ eyebrow, title, link, linkLabel }) { return <div className="home-section-heading"><div><p>{eyebrow}</p><h2>{title}</h2></div>{link && <Link to={link} className="home-text-link">{linkLabel}<ArrowRight size={16} /></Link>}</div>; }

function GameTile({ game, t }) { return <Link className="home-game-tile" to={`/games/${game.slug}`}><div className="home-game-tile__image"><MediaImage src={resolveMediaUrl(getGameArtwork(game, "cover"))} alt={`${game.title} cover`} fallbackLabel={game.title} loading="lazy" /><span>{t("home.comparePrices")}</span></div><div className="home-game-tile__body"><div className="home-card-meta">{genres(game).slice(0, 2).map((genre) => <span key={genre}>{genre}</span>)}</div><h3>{game.title}</h3><p>{game.publisher || t("home.gameStore")}</p><strong>{game.lowestPrice ? `${t("home.from")} ${formatCurrency(game.lowestPrice, game.currency || "USD")}` : t("home.viewOffers")}</strong></div></Link>; }

function ProductTile({ product, t }) { return <Link className="home-product-tile" to={product.slug ? `/products/${product.slug}` : "/top-ups"}><div className="home-product-tile__image">{product.imageUrl && <img src={resolveMediaUrl(product.imageUrl)} alt="" loading="lazy" />}<span>{t("home.digitalProduct")}</span></div><div className="home-product-tile__body"><small>{product.productType || t("home.digitalProduct")}</small><h3>{product.name}</h3><p>{product.region || "GLOBAL"}</p><strong>{formatCurrency(product.finalPrice ?? product.price, product.currency || "USD")}</strong></div></Link>; }

export default function HomePage() {
    const { t } = useTranslation();
    const [games, setGames] = useState([]);
    const [topUps, setTopUps] = useState([]);
    const [marketplace, setMarketplace] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const controller = new AbortController();
        Promise.allSettled([
            searchGames({ sortBy: "NEWEST", page: 0, size: 12, signal: controller.signal }),
            getTopUpProducts(controller.signal),
            getCatalogProducts("MARKETPLACE", controller.signal),
        ]).then(([gamesResult, topUpsResult, marketplaceResult]) => {
            if (controller.signal.aborted) return;
            if (gamesResult.status === "fulfilled") setGames(gamesResult.value?.content || gamesResult.value || []);
            if (topUpsResult.status === "fulfilled") setTopUps(topUpsResult.value?.content || topUpsResult.value || []);
            if (marketplaceResult.status === "fulfilled") setMarketplace(marketplaceResult.value?.content || marketplaceResult.value || []);
            if ([gamesResult, topUpsResult, marketplaceResult].every((item) => item.status === "rejected")) setError(t("errors.generic"));
            setLoading(false);
        });
        return () => controller.abort();
    }, [t]);

    const visibleGames = useMemo(() => games.length ? games : demoGames, [games]);
    const deals = useMemo(() => visibleGames.filter((game) => game.lowestPrice).sort((a, b) => a.lowestPrice - b.lowestPrice).slice(0, 4), [visibleGames]);
    const digitalProducts = topUps.length ? topUps : marketplace;

    return <div className="home-page">
        <HomeHeroCarousel games={visibleGames} />
        <main className="container home-content" aria-label={t("home.aria")}>
            {error && <div className="home-notice"><X size={17} />{error}</div>}
            <section className="home-section"><HomeSectionHeading eyebrow={t("home.catalogEyebrow")} title={t("home.trending")} link="/games" linkLabel={t("home.viewAll")} /><div className="home-game-grid">{visibleGames.slice(0, 4).map((game) => <GameTile key={game.id || game.slug} game={game} t={t} />)}</div></section>
            <section className="home-section home-section--deals"><HomeSectionHeading eyebrow={t("home.dealsEyebrow")} title={t("home.bestDeals")} link="/games?sortBy=PRICE_LOW_TO_HIGH" linkLabel={t("home.compareOffers")} /><div className="home-deal-grid">{deals.map((game) => <GameTile key={game.id || game.slug} game={game} t={t} />)}</div></section>
            <section className="home-section"><HomeSectionHeading eyebrow={t("home.platformEyebrow")} title={t("home.browsePlatforms")} /><div className="home-platform-grid">{platformItems.map(([name, text, Icon]) => <Link className="home-platform-tile" to={`/games?platform=${name}`} key={name}><Icon size={25} /><h3>{name}</h3><p>{text}</p><ArrowRight size={17} /></Link>)}</div></section>
            <section className="home-section"><HomeSectionHeading eyebrow={t("home.tournamentEyebrow")} title={t("home.tournaments")} link="/tournaments" linkLabel={t("home.viewAll")} /><div className="home-tournament-grid">{demoTournaments.map((item) => <article className="home-tournament" key={item.title}><div className="home-tournament__top"><Trophy size={22} /><span>{t("home.upcomingBadge")}</span></div><h3>{item.title}</h3><p>{item.game}</p><div className="home-tournament__facts"><span><CalendarDays size={14} />{item.date}</span><span><Users size={14} />{item.players}</span></div><strong>{item.prize} {t("home.prizePool")}</strong></article>)}</div></section>
            <section className="home-section home-section--products"><HomeSectionHeading eyebrow={t("home.digitalEyebrow")} title={t("home.digitalStore")} link="/top-ups" linkLabel={t("home.viewAll")} /><div className="home-product-grid">{digitalProducts.slice(0, 4).map((product) => <ProductTile key={product.id || product.slug || product.name} product={product} t={t} />)}</div></section>
            <section className="home-section home-capabilities"><HomeSectionHeading eyebrow={t("home.what")} title={t("home.heading")} /><div className="home-feature-grid"><Link className="home-feature" to="/games"><Gamepad2 size={24} /><h3>{t("home.compare")}</h3><p>{t("home.compareText")}</p><ArrowRight size={17} /></Link><Link className="home-feature" to="/top-ups"><ShoppingBag size={24} /><h3>{t("home.recharge")}</h3><p>{t("home.rechargeText")}</p><ArrowRight size={17} /></Link><Link className="home-feature" to="/marketplace"><Tag size={24} /><h3>{t("home.items")}</h3><p>{t("home.itemsText")}</p><ArrowRight size={17} /></Link><Link className="home-feature" to="/tournaments"><Trophy size={24} /><h3>{t("home.follow")}</h3><p>{t("home.followText")}</p><ArrowRight size={17} /></Link></div></section>
        </main>
        {loading && <div className="home-loading" role="status">{t("common.loading")}</div>}
    </div>;
}
