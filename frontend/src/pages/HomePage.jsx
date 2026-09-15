import { ArrowRight, CalendarDays, Gamepad2, Gift, Monitor, ShoppingBag, Tag, Trophy, Users, X } from "lucide-react";
import { Link } from "react-router-dom";
import { useEffect, useMemo, useState } from "react";

import { searchGames } from "../api/gameApi.js";
import { getActiveCatalogProducts } from "../api/catalogApi.js";
import MediaImage from "../components/MediaImage.jsx";
import HomeHeroCarousel from "../features/home/components/HomeHeroCarousel.jsx";
import { useTranslation } from "../i18n/index.jsx";
import { useMoney } from "../money/CurrencyProvider.jsx";
import { getGameArtwork } from "../utils/gameArtwork.js";
import { resolveMediaUrl } from "../utils/mediaUrl.js";
import { toSlug } from "../utils/topUps.js";
import "../styles/home.css";

const demoGames = [
    { id: "demo-cyberpunk", title: "Cyberpunk 2077", slug: "cyberpunk-2077", publisher: "CD PROJEKT RED", coverImageUrl: "/uploads/games/1/a738b9a7-3d6c-4be5-aa98-4574060cbe99.jpeg", genres: ["RPG", "Action"], releaseDate: "2020-12-10" },
    { id: "demo-rdr2", title: "Red Dead Redemption 2", slug: "red-dead-redemption-2", publisher: "Rockstar Games", coverImageUrl: "/uploads/games/6/7dd3511b-f2b5-4870-b3a9-ad62c7a5f24e.jpeg", genres: ["Action", "Adventure"], releaseDate: "2018-10-26" },
    { id: "demo-nfs", title: "Need for Speed Unbound", slug: "need-for-speed-unbound", publisher: "Criterion Games", coverImageUrl: "/uploads/games/3/12ee0127-52e2-4e64-b573-604d9c861c41.png", genres: ["Racing", "Action"], releaseDate: "2022-12-02" },
    { id: "demo-mk", title: "Mortal Kombat 1", slug: "mortal-kombat-1", publisher: "NetherRealm Studios", coverImageUrl: "/uploads/games/4/55acfdff-6b91-4257-8844-af3cff02ebf5.jpeg", genres: ["Fighting", "Action"], releaseDate: "2023-09-19" },
];

/**
 * `value` is the Platform enum the API filters on; `label` is what a shopper
 * reads. They are not the same string — sending "PlayStation" to
 * /games/search?platform= is rejected before the query ever runs.
 */
const platformItems = [
    { value: "PC", label: "PC", textKey: "home.platformPc", Icon: Monitor },
    { value: "PLAYSTATION", label: "PlayStation", textKey: "home.platformPlaystation", Icon: Gamepad2 },
    { value: "XBOX", label: "Xbox", textKey: "home.platformXbox", Icon: Gamepad2 },
    { value: "NINTENDO", label: "Nintendo", textKey: "home.platformNintendo", Icon: Gift },
];

const demoTournaments = [{ title: "GameSphere Summer Cup", game: "Counter-Strike 2", date: "2026-08-22", players: "32 / 64", prize: "$500" }, { title: "Racing Rivals", game: "Forza Horizon 5", date: "2026-08-30", players: "18 / 32", prize: "$250" }, { title: "Fighter's Arena", game: "Mortal Kombat 1", date: "2026-09-05", players: "12 / 16", prize: "$150" }];

function genres(game) { const value = game?.genres ?? game?.genre ?? []; return Array.isArray(value) ? value : String(value).split(",").filter(Boolean); }

function discountOf(product) { return Number(product.discountPercentage) || 0; }

function HomeSectionHeading({ eyebrow, title, link, linkLabel }) { return <div className="home-section-heading"><div><p>{eyebrow}</p><h2>{title}</h2></div>{link && <Link to={link} className="home-text-link">{linkLabel}<ArrowRight size={16} /></Link>}</div>; }

function GameTile({ game, t }) { return <Link className="home-game-tile" to={`/games/${game.slug}`}><div className="home-game-tile__image"><MediaImage src={resolveMediaUrl(getGameArtwork(game, "cover"))} alt={`${game.title} cover`} fallbackLabel={game.title} loading="lazy" /><span>{t("home.comparePrices")}</span></div><div className="home-game-tile__body"><div className="home-card-meta">{genres(game).slice(0, 2).map((genre) => <span key={genre}>{genre}</span>)}</div><h3>{game.title}</h3><p>{game.publisher || t("home.gameStore")}</p><strong>{t("home.viewOffers")}</strong></div></Link>; }

/**
 * A discounted offer, priced from the product row rather than the game: only
 * products carry money, so this is the one tile on the page that can show one.
 */
function DealTile({ deal, t, formatPrice }) {
    const { product, game } = deal;
    const percentage = Math.round(discountOf(product));

    return <Link className="home-game-tile home-deal-tile" to={deal.to}>
        <div className="home-game-tile__image">
            <MediaImage src={resolveMediaUrl(deal.artwork)} alt="" fallbackLabel={deal.title} loading="lazy" />
            {percentage > 0 && <em className="home-deal-badge">-{percentage}%</em>}
            <span>{product.storeName || game?.publisher || t("home.gameStore")}</span>
        </div>
        <div className="home-game-tile__body">
            <div className="home-card-meta">{product.platform && <span>{product.platform}</span>}{product.editionName && <span>{product.editionName}</span>}</div>
            <h3>{deal.title}</h3>
            <p>{product.region || t("common.global")}</p>
            <strong className="home-deal-price">
                {formatPrice(product.finalPrice ?? product.price, product.currency)}
                {product.price != null && product.finalPrice != null && Number(product.price) > Number(product.finalPrice)
                    && <s>{formatPrice(product.price, product.currency)}</s>}
            </strong>
        </div>
    </Link>;
}

/**
 * Cover art lives on the game, never on a top-up row, so the tile takes the
 * artwork its caller resolved instead of reading product.imageUrl and
 * rendering an empty frame when — as for every seeded package — it is null.
 */
function ProductTile({ entry, t, formatPrice }) {
    const { product } = entry;

    return <Link className="home-product-tile" to={entry.to}>
        <div className="home-product-tile__image">
            <MediaImage src={resolveMediaUrl(entry.artwork)} alt="" fallbackLabel={entry.gameTitle || product.name} loading="lazy" />
            <span>{t("home.digitalProduct")}</span>
        </div>
        <div className="home-product-tile__body">
            <small>{entry.gameTitle || t("home.digitalProduct")}</small>
            <h3>{product.name}</h3>
            <p>{product.region || t("common.global")}</p>
            <strong>{formatPrice(product.finalPrice ?? product.price, product.currency)}</strong>
        </div>
    </Link>;
}

export default function HomePage() {
    const { t } = useTranslation();
    const { formatPrice } = useMoney();
    const [games, setGames] = useState([]);
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const controller = new AbortController();

        Promise.allSettled([
            searchGames({ sortBy: "ALPHABETICAL", page: 0, size: 200, signal: controller.signal }),
            getActiveCatalogProducts(controller.signal),
        ]).then(([gamesResult, productsResult]) => {
            if (controller.signal.aborted) return;
            if (gamesResult.status === "fulfilled") setGames(gamesResult.value?.content || gamesResult.value || []);
            if (productsResult.status === "fulfilled") setProducts(productsResult.value || []);
            if (gamesResult.status === "rejected" && productsResult.status === "rejected") setError(t("errors.generic"));
            setLoading(false);
        });

        return () => controller.abort();
    }, [t]);

    const visibleGames = useMemo(() => (games.length ? games : demoGames), [games]);
    const gamesById = useMemo(() => new Map(visibleGames.map((game) => [game.id, game])), [visibleGames]);

    const trending = useMemo(
        () => [...visibleGames].sort((a, b) => String(b.releaseDate || "").localeCompare(String(a.releaseDate || ""))).slice(0, 4),
        [visibleGames],
    );

    /** A deal is a product that is actually discounted — deepest cut first. */
    const deals = useMemo(() => products
        .filter((product) => discountOf(product) > 0)
        .sort((a, b) => discountOf(b) - discountOf(a))
        .slice(0, 4)
        .map((product) => {
            const game = gamesById.get(product.gameId);
            const title = game?.title || product.gameTitle || product.name;
            const artwork = product.imageUrl || getGameArtwork(game, "cover");
            const to = game?.slug ? `/games/${game.slug}` : "/games";

            return { product, game, title, artwork, to };
        }), [gamesById, products]);

    const digitalProducts = useMemo(() => {
        const topUps = products.filter((product) => product.catalogSection === "TOP_UPS");
        const section = topUps.length ? topUps : products.filter((product) => product.catalogSection === "MARKETPLACE");

        return section.slice(0, 4).map((product) => {
            const game = gamesById.get(product.gameId);
            const gameTitle = game?.title || product.gameTitle || "";
            const artwork = product.imageUrl || getGameArtwork(game, "cover");
            const slug = game?.slug || (gameTitle ? toSlug(gameTitle) : "");

            return {
                product,
                gameTitle,
                artwork,
                // There is no /products/:slug route; a package belongs to its game.
                to: product.catalogSection === "TOP_UPS" && slug ? `/top-ups/${slug}` : "/marketplace",
            };
        });
    }, [gamesById, products]);

    return <div className="home-page">
        <HomeHeroCarousel games={visibleGames} />
        <main className="container home-content" aria-label={t("home.aria")}>
            {error && <div className="home-notice"><X size={17} />{error}</div>}
            <section className="home-section"><HomeSectionHeading eyebrow={t("home.catalogEyebrow")} title={t("home.trending")} link="/games" linkLabel={t("home.viewAll")} /><div className="home-game-grid">{trending.map((game) => <GameTile key={game.id || game.slug} game={game} t={t} />)}</div></section>
            {deals.length > 0 && <section className="home-section home-section--deals"><HomeSectionHeading eyebrow={t("home.dealsEyebrow")} title={t("home.bestDeals")} link="/games?sortBy=PRICE_LOW_TO_HIGH" linkLabel={t("home.compareOffers")} /><div className="home-deal-grid">{deals.map((deal) => <DealTile key={deal.product.id} deal={deal} t={t} formatPrice={formatPrice} />)}</div></section>}
            <section className="home-section"><HomeSectionHeading eyebrow={t("home.platformEyebrow")} title={t("home.browsePlatforms")} /><div className="home-platform-grid">{platformItems.map(({ value, label, textKey, Icon }) => <Link className="home-platform-tile" to={`/games?platform=${value}`} key={value}><Icon size={25} /><h3>{label}</h3><p>{t(textKey)}</p><ArrowRight size={17} /></Link>)}</div></section>
            <section className="home-section"><HomeSectionHeading eyebrow={t("home.tournamentEyebrow")} title={t("home.tournaments")} link="/tournaments" linkLabel={t("home.viewAll")} /><div className="home-tournament-grid">{demoTournaments.map((item) => <article className="home-tournament" key={item.title}><div className="home-tournament__top"><Trophy size={22} /><span>{t("home.upcomingBadge")}</span></div><h3>{item.title}</h3><p>{item.game}</p><div className="home-tournament__facts"><span><CalendarDays size={14} />{item.date}</span><span><Users size={14} />{item.players}</span></div><strong>{item.prize} {t("home.prizePool")}</strong></article>)}</div></section>
            {digitalProducts.length > 0 && <section className="home-section home-section--products"><HomeSectionHeading eyebrow={t("home.digitalEyebrow")} title={t("home.digitalStore")} link="/top-ups" linkLabel={t("home.viewAll")} /><div className="home-product-grid">{digitalProducts.map((entry) => <ProductTile key={entry.product.id} entry={entry} t={t} formatPrice={formatPrice} />)}</div></section>}
            <section className="home-section home-capabilities"><HomeSectionHeading eyebrow={t("home.what")} title={t("home.heading")} /><div className="home-feature-grid"><Link className="home-feature" to="/games"><Gamepad2 size={24} /><h3>{t("home.compare")}</h3><p>{t("home.compareText")}</p><ArrowRight size={17} /></Link><Link className="home-feature" to="/top-ups"><ShoppingBag size={24} /><h3>{t("home.recharge")}</h3><p>{t("home.rechargeText")}</p><ArrowRight size={17} /></Link><Link className="home-feature" to="/marketplace"><Tag size={24} /><h3>{t("home.items")}</h3><p>{t("home.itemsText")}</p><ArrowRight size={17} /></Link><Link className="home-feature" to="/tournaments"><Trophy size={24} /><h3>{t("home.follow")}</h3><p>{t("home.followText")}</p><ArrowRight size={17} /></Link></div></section>
        </main>
        {loading && <div className="home-loading" role="status">{t("common.loading")}</div>}
    </div>;
}
