// import { RouterProvider } from "react-router-dom";
//
// import { router } from "./router.jsx";
//
// export default function App() {
//     return <RouterProvider router={router} />;
// }


import {
    ArrowLeft,
    ArrowRight,
    CircleUserRound,
    Check,
    ChevronDown,
    ExternalLink,
    Gamepad2,
    Heart,
    KeyRound,
    LoaderCircle,
    LogIn,
    LogOut,
    Menu,
    Monitor,
    PackageCheck,
    Search,
    ShieldCheck,
    ShoppingBag,
    Smartphone,
    Sparkles,
    Store,
    Tag,
    Trash2,
    UserRound,
    X,
} from "lucide-react";
import { useEffect, useMemo, useRef, useState } from "react";

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
const FALLBACK_ART = "/assets/gamesphere-worlds.png";
const BASE_GAME_PRODUCT_TYPES = new Set(["GAME", "DLC", "BUNDLE"]);

const GAME_GENRES = [
    "ACTION",
    "ADVENTURE",
    "FPS",
    "RPG",
    "RACING",
    "SPORTS",
    "STRATEGY",
    "HORROR",
    "SURVIVAL",
    "MMO",
    "SIMULATION",
    "FIGHTING",
];

const GAME_PLATFORMS = ["PC", "PLAYSTATION", "XBOX", "NINTENDO", "MOBILE"];

const DEFAULT_CATALOG_FILTERS = {
    accessType: "",
    genre: "",
    platform: "",
    sortBy: "ALPHABETICAL",
};

const DEMO_GAMES = [
    {
        id: "demo-cyberpunk-2077",
        title: "Cyberpunk 2077",
        slug: "cyberpunk-2077",
        description: "Compare official PC storefront prices for Night City.",
        developer: "CD PROJEKT RED",
        publisher: "CD PROJEKT RED",
        releaseDate: "2020-12-10",
        coverImageUrl: "/uploads/games/1/a738b9a7-3d6c-4be5-aa98-4574060cbe99.jpeg",
        lowestPrice: 39.99,
        offerCount: 2,
        accessType: "PAID",
        genres: ["RPG", "ACTION"],
        supportedPlatforms: ["PC"],
        demoOffers: [
            { storeName: "Steam", storeUrl: "https://store.steampowered.com/app/1091500/Cyberpunk_2077/", price: 59.99, finalPrice: 39.99 },
            { storeName: "Epic Games Store", storeUrl: "https://store.epicgames.com/p/cyberpunk-2077", price: 59.99, finalPrice: 44.99 },
        ],
        demo: true,
    },
    {
        id: "demo-cod-modern-warfare-iii",
        title: "Call of Duty: Modern Warfare III",
        slug: "call-of-duty-modern-warfare-iii",
        description: "Compare PC offers across Steam and Battle.net.",
        developer: "Sledgehammer Games",
        publisher: "Activision",
        releaseDate: "2023-11-10",
        coverImageUrl: "/uploads/games/2/e24f05db-6bb6-44d2-8393-62c41d772d45.png",
        lowestPrice: 49.99,
        offerCount: 2,
        accessType: "PAID",
        genres: ["FPS", "ACTION"],
        supportedPlatforms: ["PC"],
        demoOffers: [
            { storeName: "Steam", storeUrl: "https://store.steampowered.com/app/3595270/Call_of_Duty_Modern_Warfare_III/", price: 69.99, finalPrice: 49.99 },
            { storeName: "Battle.net", storeUrl: "https://us.shop.battle.net/en-us/product/call-of-duty-modern-warfare-iii#optLogin=true", price: 69.99, finalPrice: 49.99 },
        ],
        demo: true,
    },
    {
        id: "demo-nfs-unbound",
        title: "Need for Speed Unbound",
        slug: "need-for-speed-unbound",
        description: "Find the official EA App offer for this street racing game.",
        developer: "Criterion Games",
        publisher: "Electronic Arts",
        releaseDate: "2022-12-02",
        coverImageUrl: "/uploads/games/3/12ee0127-52e2-4e64-b573-604d9c861c41.png",
        lowestPrice: 24.99,
        offerCount: 1,
        accessType: "PAID",
        genres: ["RACING", "ACTION"],
        supportedPlatforms: ["PC"],
        demoOffers: [
            { storeName: "EA App", storeUrl: "https://www.ea.com/games/need-for-speed/need-for-speed-unbound", price: 49.99, finalPrice: 24.99 },
        ],
        demo: true,
    },
    {
        id: "demo-mortal-kombat-1",
        title: "Mortal Kombat 1",
        slug: "mortal-kombat-1",
        description: "Compare the official PC Steam offer before continuing to store.",
        developer: "NetherRealm Studios",
        publisher: "Warner Bros. Games",
        releaseDate: "2023-09-19",
        coverImageUrl: "/uploads/games/4/55acfdff-6b91-4257-8844-af3cff02ebf5.jpeg",
        lowestPrice: 39.99,
        offerCount: 1,
        accessType: "PAID",
        genres: ["FIGHTING", "ACTION"],
        supportedPlatforms: ["PC"],
        demoOffers: [
            { storeName: "Steam", storeUrl: "https://store.steampowered.com/app/1971870/Mortal_Kombat_1/", price: 69.99, finalPrice: 39.99 },
        ],
        demo: true,
    },
    {
        id: "demo-forza-horizon-6",
        title: "Forza Horizon 6",
        slug: "forza-horizon-6",
        description: "Compare the PC-compatible Xbox Store offer.",
        developer: "Playground Games",
        publisher: "Xbox Game Studios",
        coverImageUrl: "/uploads/games/5/0ff36480-6fec-4b1e-a384-06f9f40e57db.jpeg",
        lowestPrice: 59.99,
        offerCount: 1,
        accessType: "PAID",
        genres: ["RACING", "SIMULATION"],
        supportedPlatforms: ["PC"],
        demoOffers: [
            { storeName: "Xbox Store", storeUrl: "https://www.xbox.com/en-US/games/store/forza-horizon-6/9NR1R1XWLCNB/0010", price: 69.99, finalPrice: 59.99 },
        ],
        demo: true,
    },
    {
        id: "demo-red-dead-redemption-2",
        title: "Red Dead Redemption 2",
        slug: "red-dead-redemption-2",
        description: "Compare the direct PC Steam offer for the western epic.",
        developer: "Rockstar Games",
        publisher: "Rockstar Games",
        releaseDate: "2018-10-26",
        coverImageUrl: "/uploads/games/6/7dd3511b-f2b5-4870-b3a9-ad62c7a5f24e.jpeg",
        lowestPrice: 19.99,
        offerCount: 1,
        accessType: "PAID",
        genres: ["ACTION", "ADVENTURE"],
        supportedPlatforms: ["PC"],
        demoOffers: [
            { storeName: "Steam", storeUrl: "https://store.steampowered.com/app/1174180/Red_Dead_Redemption_2/", price: 59.99, finalPrice: 19.99 },
        ],
        demo: true,
    },
];

const DEMO_PRODUCTS = [
    {
        id: "demo-vp-2050",
        name: "2050 Valorant Points",
        gameTitle: "Valorant",
        productType: "IN_GAME_CURRENCY",
        inGameCurrencyName: "VP",
        inGameAmount: 1900,
        bonusAmount: 150,
        storeName: "Official top-up",
        storeUrl: "https://playvalorant.com/",
        finalPrice: 19.99,
        currency: "USD",
        imageUrl: FALLBACK_ART,
    },
    {
        id: "demo-uc-1800",
        name: "1800 PUBG Mobile UC",
        gameTitle: "PUBG Mobile",
        productType: "IN_GAME_CURRENCY",
        inGameCurrencyName: "UC",
        inGameAmount: 1650,
        bonusAmount: 150,
        storeName: "Global provider",
        storeUrl: "https://www.pubgmobile.com/",
        finalPrice: 39.42,
        currency: "USD",
        imageUrl: FALLBACK_ART,
    },
    {
        id: "demo-cp-2400",
        name: "2400 COD Points",
        gameTitle: "Call of Duty: Warzone",
        productType: "IN_GAME_CURRENCY",
        inGameCurrencyName: "CP",
        inGameAmount: 2000,
        bonusAmount: 400,
        storeName: "Platform store",
        storeUrl: "https://www.callofduty.com/",
        finalPrice: 19.99,
        currency: "USD",
        imageUrl: FALLBACK_ART,
    },
    {
        id: "demo-delta-coins",
        name: "3950 Delta Coins",
        gameTitle: "Delta Force",
        productType: "IN_GAME_CURRENCY",
        inGameCurrencyName: "Delta Coins",
        inGameAmount: 3280,
        bonusAmount: 670,
        storeName: "UID top-up",
        storeUrl: "https://www.playdeltaforce.com/",
        finalPrice: 49.92,
        currency: "USD",
        imageUrl: FALLBACK_ART,
    },
    {
        id: "demo-cs2-item",
        name: "M4A1-S | Decimator",
        gameTitle: "Counter-Strike 2",
        productType: "IN_GAME_ITEM",
        storeName: "Steam Community Market",
        storeUrl: "https://steamcommunity.com/market/",
        finalPrice: 16.75,
        currency: "USD",
        imageUrl: FALLBACK_ART,
    },
    {
        id: "demo-dota-item",
        name: "Hero Arcana Set",
        gameTitle: "Dota 2",
        productType: "IN_GAME_ITEM",
        storeName: "External marketplace",
        storeUrl: "https://steamcommunity.com/market/",
        finalPrice: 31.2,
        currency: "USD",
        imageUrl: FALLBACK_ART,
    },
    {
        id: "demo-tf2-item",
        name: "Mann Co. Supply Crate Key",
        gameTitle: "Team Fortress 2",
        productType: "IN_GAME_ITEM",
        storeName: "Steam Community Market",
        storeUrl: "https://steamcommunity.com/market/",
        finalPrice: 2.19,
        currency: "USD",
        imageUrl: FALLBACK_ART,
    },
    {
        id: "demo-finals-item",
        name: "Starter Cosmetic Bundle",
        gameTitle: "The Finals",
        productType: "IN_GAME_ITEM",
        storeName: "Official store",
        storeUrl: "https://www.reachthefinals.com/",
        finalPrice: 9.99,
        currency: "USD",
        imageUrl: FALLBACK_ART,
    },
];

const tabs = [
    { id: "games", label: "Games", icon: Gamepad2 },
    { id: "topups", label: "Top-ups", icon: Sparkles },
    { id: "market", label: "Marketplace", icon: Tag },
];

const platformIcons = {
    PC: Monitor,
    PLAYSTATION: Gamepad2,
    XBOX: Gamepad2,
    NINTENDO: Gamepad2,
    MOBILE: Smartphone,
};

function resolveImage(url) {
    if (!url || url.includes("example.com")) return FALLBACK_ART;
    if (url.startsWith("/assets/")) return url;
    if (url.startsWith("/")) return `${API_BASE}${url}`;
    return url;
}

function gameImage(game) {
    return resolveImage(
        game?.coverImageUrl ||
        game?.cover_image_url ||
        game?.imageUrl ||
        game?.image_url,
    );
}

function currency(amount, code = "USD") {
    if (amount === null || amount === undefined) return "No offer";
    try {
        return new Intl.NumberFormat("en-US", {
            style: "currency",
            currency: code || "USD",
            maximumFractionDigits: 2,
        }).format(amount);
    } catch {
        return `${amount} ${code || "USD"}`;
    }
}

function formatEnum(value) {
    return value
        ? value
            .toLowerCase()
            .split("_")
            .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
            .join(" ")
        : "";
}

function isBaseGameOffer(offer) {
    return BASE_GAME_PRODUCT_TYPES.has(offer.productType);
}

function buildCatalogSearchUrl(query, filters) {
    const params = new URLSearchParams({ page: "0", size: "50" });
    if (query.trim()) params.set("q", query.trim());
    if (filters.accessType) params.set("accessType", filters.accessType);
    if (filters.genre) params.append("genres", filters.genre);
    if (filters.platform) params.set("platform", filters.platform);
    if (filters.sortBy) params.set("sortBy", filters.sortBy);
    return `${API_BASE}/api/games/search?${params.toString()}`;
}

function imagePosition(index) {
    return ["20% center", "52% center", "86% center"][index % 3];
}

function createDemoComparison(game) {
    const basePrice = game.lowestPrice;
    const stores = (game.demoOffers || [
        {
            storeName: "Steam",
            platform: "PC",
            price: basePrice + 10,
            finalPrice: basePrice,
            officialStore: true,
            deliveryType: "STORE_REDIRECT",
            keyProvider: "Direct account activation",
            storeUrl: "https://store.steampowered.com/",
        },
        {
            storeName: "Xbox Store",
            platform: "XBOX",
            price: basePrice + 15,
            finalPrice: basePrice + 6,
            officialStore: true,
            deliveryType: "STORE_REDIRECT",
            keyProvider: "Xbox account",
            storeUrl: "https://www.xbox.com/games/store",
        },
        {
            storeName: "PlayStation Store",
            platform: "PLAYSTATION",
            price: basePrice + 18,
            finalPrice: basePrice + 9,
            officialStore: true,
            deliveryType: "STORE_REDIRECT",
            keyProvider: "PlayStation account",
            storeUrl: "https://store.playstation.com/",
        },
    ]).map((offer) => ({
        platform: "PC",
        officialStore: true,
        deliveryType: "STORE_REDIRECT",
        keyProvider: offer.storeName,
        ...offer,
    })).slice(0, game.offerCount);

    return {
        game,
        lowestPrice: basePrice,
        offerCount: stores.length,
        availablePlatforms: [...new Set(stores.map((offer) => offer.platform))],
        offers: stores.map((offer, index) => ({
            ...offer,
            productId: `${game.id}-offer-${index}`,
            productName: `${game.title} Standard Edition`,
            productSlug: `${game.slug}-${offer.platform.toLowerCase()}`,
            region: "GLOBAL",
            currency: "USD",
            productType: "GAME",
            status: "ACTIVE",
            discountPrice:
                offer.price > offer.finalPrice ? offer.finalPrice : null,
            stockQuantity: 100,
        })),
    };
}

function App() {
    const [activeTab, setActiveTab] = useState("games");
    const [query, setQuery] = useState("");
    const [games, setGames] = useState([]);
    const [products, setProducts] = useState([]);
    const [selectedGame, setSelectedGame] = useState(null);
    const [comparison, setComparison] = useState(null);
    const [platform, setPlatform] = useState("ALL");
    const [catalogFilters, setCatalogFilters] = useState(DEFAULT_CATALOG_FILTERS);
    const [loading, setLoading] = useState(true);
    const [productLoading, setProductLoading] = useState(false);
    const [detailLoading, setDetailLoading] = useState(false);
    const [error, setError] = useState("");
    const [gameSource, setGameSource] = useState("live");
    const [productSource, setProductSource] = useState("live");
    const [mobileNavOpen, setMobileNavOpen] = useState(false);
    const [activePopover, setActivePopover] = useState(null);
    const [wishlist, setWishlist] = useState([]);
    const [cart, setCart] = useState([]);
    const [profile, setProfile] = useState(null);
    const [accountLoading, setAccountLoading] = useState(false);
    const [accountError, setAccountError] = useState("");
    const popoverRef = useRef(null);
    const accessToken = localStorage.getItem("accessToken") || localStorage.getItem("gamesphere.accessToken");
    const isAuthenticated = Boolean(accessToken);

    useEffect(() => {
        function closeOnOutsideClick(event) {
            if (popoverRef.current && !popoverRef.current.contains(event.target)) setActivePopover(null);
        }
        function closeOnEscape(event) {
            if (event.key === "Escape") setActivePopover(null);
        }
        document.addEventListener("mousedown", closeOnOutsideClick);
        document.addEventListener("keydown", closeOnEscape);
        return () => {
            document.removeEventListener("mousedown", closeOnOutsideClick);
            document.removeEventListener("keydown", closeOnEscape);
        };
    }, []);

    async function togglePopover(nextPopover) {
        if (activePopover === nextPopover) {
            setActivePopover(null);
            return;
        }

        setActivePopover(nextPopover);
        setAccountError("");
        if (!isAuthenticated) return;

        const endpoints = { wishlist: "/api/wishlist", cart: "/api/cart", profile: "/api/users/profile" };
        setAccountLoading(true);
        try {
            const response = await fetch(`${API_BASE}${endpoints[nextPopover]}`, {
                headers: { Authorization: `Bearer ${accessToken}` },
            });
            if (!response.ok) throw new Error("Account data could not be loaded.");
            const payload = await response.json();
            if (nextPopover === "wishlist") setWishlist(payload.data?.products || []);
            if (nextPopover === "cart") setCart(payload.data?.items || []);
            if (nextPopover === "profile") setProfile(payload.data || null);
        } catch {
            setAccountError("We could not load your account data right now.");
        } finally {
            setAccountLoading(false);
        }
    }

    async function removeWishlistItem(productId) {
        if (!isAuthenticated) return;
        const response = await fetch(`${API_BASE}/api/wishlist/remove/${productId}`, {
            method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` },
        });
        if (!response.ok) { setAccountError("The wishlist item could not be removed."); return; }
        const payload = await response.json();
        setWishlist(payload.data?.products || []);
    }

    async function removeCartItem(productId) {
        if (!isAuthenticated) return;
        const response = await fetch(`${API_BASE}/api/cart/items/${productId}`, {
            method: "DELETE", headers: { Authorization: `Bearer ${accessToken}` },
        });
        if (!response.ok) { setAccountError("The cart item could not be removed."); return; }
        const payload = await response.json();
        setCart(payload.data?.items || []);
    }

    async function loadGames(signal) {
        setLoading(true);
        setError("");
        try {
            const response = await fetch(buildCatalogSearchUrl(query, catalogFilters), {
                signal,
            });
            if (!response.ok) throw new Error("Game catalog could not be loaded.");
            const payload = await response.json();
            const content = payload.data?.content || [];
            if (content.length === 0) {
                setGames(DEMO_GAMES);
                setGameSource("demo");
            } else {
                setGames(content);
                setGameSource("live");
            }
        } catch (requestError) {
            if (requestError.name === "AbortError") return;
            setGames(DEMO_GAMES);
            setGameSource("demo");
        } finally {
            setLoading(false);
        }
    }

    async function loadProducts() {
        setProductLoading(true);
        setError("");
        try {
            const response = await fetch(
                `${API_BASE}/api/products?status=ACTIVE&inStock=true&page=0&size=50`,
            );
            if (!response.ok) throw new Error("Products could not be loaded.");
            const payload = await response.json();
            const content = payload.data?.content || [];
            if (content.length === 0) {
                setProducts(DEMO_PRODUCTS);
                setProductSource("demo");
            } else {
                setProducts(content);
                setProductSource("live");
            }
        } catch {
            setProducts(DEMO_PRODUCTS);
            setProductSource("demo");
        } finally {
            setProductLoading(false);
        }
    }

    useEffect(() => {
        const controller = new AbortController();
        const timer = window.setTimeout(() => loadGames(controller.signal), 250);

        return () => {
            controller.abort();
            window.clearTimeout(timer);
        };
    }, [query, catalogFilters]);

    useEffect(() => {
        if (activeTab === "games" || products.length !== 0) return undefined;

        const timer = window.setTimeout(() => {
            void loadProducts();
        }, 0);

        return () => window.clearTimeout(timer);
    }, [activeTab, products.length]);

    async function openGame(game) {
        setActivePopover(null);
        setSelectedGame(game);
        setPlatform("ALL");
        setComparison(null);
        setDetailLoading(true);
        window.scrollTo({ top: 0, behavior: "smooth" });

        if (game.demo) {
            setComparison(createDemoComparison(game));
            setDetailLoading(false);
            return;
        }

        try {
            const response = await fetch(
                `${API_BASE}/api/games/slug/${game.slug}/offers?sort=PRICE_ASC`,
            );
            if (!response.ok) throw new Error("Offers could not be loaded.");
            const payload = await response.json();
            setComparison(payload.data);
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setDetailLoading(false);
        }
    }

    const filteredGames = useMemo(() => {
        if (gameSource !== "demo") return games;

        const normalized = query.trim().toLowerCase();
        return games.filter((game) => {
            const textMatches =
                !normalized ||
                [game.title, game.developer, game.publisher]
                    .filter(Boolean)
                    .some((value) => value.toLowerCase().includes(normalized));
            const typeMatches =
                !catalogFilters.accessType || game.accessType === catalogFilters.accessType;
            const genreMatches =
                !catalogFilters.genre || game.genres?.includes(catalogFilters.genre);
            const platformMatches =
                !catalogFilters.platform || game.supportedPlatforms?.includes(catalogFilters.platform);

            return textMatches && typeMatches && genreMatches && platformMatches;
        });
    }, [games, query, gameSource, catalogFilters]);

    const filteredProducts = useMemo(() => {
        const type =
            activeTab === "topups" ? "IN_GAME_CURRENCY" : "IN_GAME_ITEM";
        const normalized = query.trim().toLowerCase();
        return products.filter(
            (product) =>
                product.productType === type &&
                (!normalized ||
                    [product.name, product.gameTitle, product.storeName]
                        .filter(Boolean)
                        .some((value) => value.toLowerCase().includes(normalized))),
        );
    }, [products, activeTab, query]);

    const featured =
        games.find((game) => game.slug === "battlefield-6") || games[0];

    const filteredOffers = useMemo(() => {
        const offers = comparison?.offers || [];
        if (platform === "ALL") return offers;
        return offers.filter((offer) => offer.platform === platform);
    }, [comparison, platform]);

    function changeTab(tab) {
        setActivePopover(null);
        setActiveTab(tab);
        setSelectedGame(null);
        setMobileNavOpen(false);
        setQuery("");
        setCatalogFilters(DEFAULT_CATALOG_FILTERS);
    }

    return (
        <div className="app-shell">
            <Header
                activeTab={activeTab}
                changeTab={changeTab}
                query={query}
                setQuery={setQuery}
                mobileNavOpen={mobileNavOpen}
                setMobileNavOpen={setMobileNavOpen}
                onTogglePopover={togglePopover}
                activePopover={activePopover}
                popoverRef={popoverRef}
                isAuthenticated={isAuthenticated}
                profile={profile}
                loading={accountLoading}
                error={accountError}
                wishlistCount={wishlist.length}
                cartCount={cart.reduce((total, item) => total + (item.quantity || 1), 0)}
                wishlist={wishlist}
                cart={cart}
                onRemoveWishlist={removeWishlistItem}
                onRemoveCart={removeCartItem}
                onBrowseGames={() => changeTab("games")}
                onBrowseMarketplace={() => changeTab("market")}
            />

            <main>
                {selectedGame ? (
                    <GameDetail
                        game={selectedGame}
                        comparison={comparison}
                        offers={filteredOffers}
                        platform={platform}
                        setPlatform={setPlatform}
                        loading={detailLoading}
                        onBack={() => setSelectedGame(null)}
                    />
                ) : activeTab === "games" ? (
                    <GameCatalog
                        featured={featured}
                        games={filteredGames}
                        loading={loading}
                        error={error}
                        demo={gameSource === "demo"}
                        filters={catalogFilters}
                        onFiltersChange={setCatalogFilters}
                        onResetFilters={() => setCatalogFilters(DEFAULT_CATALOG_FILTERS)}
                        onOpenGame={openGame}
                    />
                ) : (
                    <ProductCatalog
                        tab={activeTab}
                        products={filteredProducts}
                        loading={productLoading}
                        error={error}
                        demo={productSource === "demo"}
                    />
                )}
            </main>

            <footer>
                <div className="footer-brand">
                    <BrandMark />
                    <span>GameSphere</span>
                </div>
                <p>One catalog. Every platform. The clearest price.</p>
                <span>Course project preview</span>
            </footer>
        </div>
    );
}

function Header({
                    activeTab,
                    changeTab,
                    query,
                    setQuery,
                    mobileNavOpen,
                    setMobileNavOpen,
                    onTogglePopover,
                    activePopover,
                    popoverRef,
                    isAuthenticated,
                    profile,
                    loading,
                    error,
                    wishlistCount,
                    cartCount,
                    wishlist,
                    cart,
                    onRemoveWishlist,
                    onRemoveCart,
                    onBrowseGames,
                    onBrowseMarketplace,
                }) {
    return (
        <header className="topbar">
            <button
                className="icon-button mobile-menu"
                onClick={() => setMobileNavOpen(!mobileNavOpen)}
                aria-label="Toggle navigation"
            >
                {mobileNavOpen ? <X size={20} /> : <Menu size={20} />}
            </button>

            <button className="brand" onClick={() => changeTab("games")}>
                <BrandMark />
                <span>GameSphere</span>
            </button>

            <nav className={mobileNavOpen ? "nav-open" : ""}>
                {tabs.map((tab) => {
                    const Icon = tab.icon;
                    return (
                        <button
                            key={tab.id}
                            className={activeTab === tab.id ? "nav-item active" : "nav-item"}
                            onClick={() => changeTab(tab.id)}
                        >
                            <Icon size={17} />
                            {tab.label}
                        </button>
                    );
                })}
            </nav>

            <label className="search-box">
                <Search size={18} />
                <input
                    value={query}
                    onChange={(event) => setQuery(event.target.value)}
                    placeholder="Search games, currency, items..."
                />
                <kbd>/</kbd>
            </label>

            <div className="header-actions" ref={popoverRef}>
                <button className={activePopover === "wishlist" ? "icon-button active" : "icon-button"} aria-label="Wishlist" aria-expanded={activePopover === "wishlist"} title="Wishlist" onClick={() => onTogglePopover("wishlist")}>
                    <Heart size={19} />
                    {wishlistCount > 0 && <span className="action-count">{wishlistCount}</span>}
                </button>
                <button className={activePopover === "cart" ? "icon-button active" : "icon-button"} aria-label="Cart" aria-expanded={activePopover === "cart"} title="Cart" onClick={() => onTogglePopover("cart")}>
                    <ShoppingBag size={19} />
                    {cartCount > 0 && <span className="action-count">{cartCount}</span>}
                </button>
                <button className={activePopover === "profile" ? "profile-button active" : "profile-button"} aria-label="Profile menu" aria-expanded={activePopover === "profile"} onClick={() => onTogglePopover("profile")}>
          <span className="avatar">
            <UserRound size={17} />
          </span>
                    <span className="profile-copy">
            <strong>{profile?.firstName || profile?.username || "Account"}</strong>
            <small>{isAuthenticated ? "My account" : "Sign in"}</small>
          </span>
                    <ChevronDown size={15} />
                </button>
                {activePopover && <HeaderPopover
                    type={activePopover}
                    authenticated={isAuthenticated}
                    profile={profile}
                    wishlist={wishlist}
                    cart={cart}
                    loading={loading}
                    error={error}
                    onRemoveWishlist={onRemoveWishlist}
                    onRemoveCart={onRemoveCart}
                    onBrowseGames={onBrowseGames}
                    onBrowseMarketplace={onBrowseMarketplace}
                    onSelect={onTogglePopover}
                />}
            </div>
        </header>
    );
}

function BrandMark() {
    return (
        <span className="brand-mark" aria-hidden="true">
      G
    </span>
    );
}

function GameCatalog({
                         featured,
                         games,
                         loading,
                         error,
                         demo,
                         filters,
                         onFiltersChange,
                         onResetFilters,
                         onOpenGame,
                     }) {
    return (
        <>
            {featured && (
                <section
                    className="feature-band"
                    style={{
                        backgroundImage: `url("${gameImage(featured)}")`,
                    }}
                >
                    <div className="feature-shade" />
                    <div className="feature-content">
            <span className="eyebrow">
              <ShieldCheck size={15} />
              Price checked across stores
            </span>
                        <h1>{featured.title}</h1>
                        <p>
                            Compare official stores, digital keys and platform editions in
                            one clean view.
                        </p>
                        <div className="feature-actions">
                            <button
                                className="primary-button"
                                onClick={() => onOpenGame(featured)}
                            >
                                Compare offers
                                <ArrowRight size={18} />
                            </button>
                            <span className="feature-meta">
                {featured.developer || "Game studio"} {" / "}
                                {featured.releaseDate || "Release date TBA"}
              </span>
                        </div>
                    </div>
                </section>
            )}

            <section className="catalog-section">
                <div className="section-heading">
                    <div>
                        <span className="section-kicker">CATALOG</span>
                        <h2>Browse games</h2>
                    </div>
                    <div className="catalog-status">
                        <span className="live-dot" />
                        {games.length} titles available
                    </div>
                </div>

                <CatalogFilters
                    filters={filters}
                    onChange={onFiltersChange}
                    onReset={onResetFilters}
                />

                {demo && (
                    <div className="demo-notice">
                        <Sparkles size={16} />
                        Demo catalog is shown because the API has no game records yet.
                    </div>
                )}

                {loading ? (
                    <LoadingState label="Loading game catalog..." />
                ) : error ? (
                    <EmptyState title="Backend is out of reach" description={error} />
                ) : games.length === 0 ? (
                    <EmptyState
                        title="No games found"
                        description="Try another search phrase."
                    />
                ) : (
                    <div className="game-grid">
                        {games.map((game, index) => (
                            <GameCard
                                key={game.id}
                                game={game}
                                index={index}
                                onClick={() => onOpenGame(game)}
                            />
                        ))}
                    </div>
                )}
            </section>
        </>
    );
}

function GameCard({ game, index, onClick }) {
    return (
        <article className="game-card" onClick={onClick}>
            <div className="game-art">
                <img
                    src={gameImage(game)}
                    alt={`${game.title} cover`}
                    style={{ objectPosition: imagePosition(index) }}
                    onError={(event) => {
                        event.currentTarget.src = FALLBACK_ART;
                    }}
                />
                <button
                    className="favorite-button"
                    aria-label={`Add ${game.title} to wishlist`}
                    onClick={(event) => event.stopPropagation()}
                >
                    <Heart size={17} />
                </button>
                {game.offerCount && (
                    <span className="offer-pill">{game.offerCount} offers</span>
                )}
            </div>
            <div className="game-card-body">
                <div>
                    <div className="game-labels">
            <span className={game.accessType === "FREE_TO_PLAY" ? "game-tag f2p" : "game-tag"}>
              {game.accessType === "FREE_TO_PLAY" ? "Free to play" : "Paid"}
            </span>
                        {game.genres?.slice(0, 2).map((genre) => (
                            <span className="game-tag" key={genre}>{formatEnum(genre)}</span>
                        ))}
                    </div>
                    <h3>{game.title}</h3>
                    <p>{game.developer || game.publisher || "Independent studio"}</p>
                </div>
                <div className="price-row">
                    {game.accessType === "FREE_TO_PLAY" ? (
                        <span>Free base game</span>
                    ) : game.lowestPrice !== undefined ? (
                        <span>
              from <strong>{currency(game.lowestPrice)}</strong>
            </span>
                    ) : (
                        <span>Compare prices</span>
                    )}
                    <ArrowRight size={17} />
                </div>
            </div>
        </article>
    );
}

function CatalogFilters({ filters, onChange, onReset }) {
    function update(name, value) {
        onChange((current) => {
            const next = { ...current, [name]: value };
            if (name === "accessType" && value === "FREE_TO_PLAY") {
                next.sortBy = "ALPHABETICAL";
            }
            return next;
        });
    }

    const showPriceSort = filters.accessType !== "FREE_TO_PLAY";

    return (
        <div className="catalog-filters" aria-label="Game catalog filters">
            <label>
                <span>Game type</span>
                <select value={filters.accessType} onChange={(event) => update("accessType", event.target.value)}>
                    <option value="">All games</option>
                    <option value="FREE_TO_PLAY">Free to play</option>
                    <option value="PAID">Paid games</option>
                </select>
            </label>
            <label>
                <span>Genre</span>
                <select value={filters.genre} onChange={(event) => update("genre", event.target.value)}>
                    <option value="">All genres</option>
                    {GAME_GENRES.map((genre) => <option key={genre} value={genre}>{formatEnum(genre)}</option>)}
                </select>
            </label>
            <label>
                <span>Platform</span>
                <select value={filters.platform} onChange={(event) => update("platform", event.target.value)}>
                    <option value="">All platforms</option>
                    {GAME_PLATFORMS.map((item) => <option key={item} value={item}>{formatEnum(item)}</option>)}
                </select>
            </label>
            <label>
                <span>Sort by</span>
                <select value={filters.sortBy} onChange={(event) => update("sortBy", event.target.value)}>
                    <option value="ALPHABETICAL">Alphabetical</option>
                    <option value="NEWEST">Newest</option>
                    <option value="OLDEST">Oldest</option>
                    {showPriceSort && <option value="PRICE_LOW_TO_HIGH">Price: Low to high</option>}
                    {showPriceSort && <option value="PRICE_HIGH_TO_LOW">Price: High to low</option>}
                </select>
            </label>
            <button className="filter-reset" onClick={onReset}>Reset</button>
        </div>
    );
}

function GameDetail({
                        game,
                        comparison,
                        offers,
                        platform,
                        setPlatform,
                        loading,
                        onBack,
                    }) {
    const allOffers = offers || [];
    const baseGameOffers = allOffers.filter(isBaseGameOffer);
    const marketplaceOffers = allOffers.filter((offer) => !isBaseGameOffer(offer));
    const platforms = comparison?.availablePlatforms || [];
    const hasPlatformChoice = platforms.length > 1;
    const freeToPlay = game.accessType === "FREE_TO_PLAY";

    return (
        <div className="detail-view">
            <section
                className="detail-hero"
                style={{
                    backgroundImage: `url("${gameImage(game)}")`,
                }}
            >
                <div className="detail-shade" />
                <button className="back-button" onClick={onBack}>
                    <ArrowLeft size={18} />
                    All games
                </button>
                <div className="detail-copy">
                    <div className="detail-tags">
                        <span>{game.developer || "Studio"}</span>
                        <span>{game.releaseDate || "Release TBA"}</span>
                        <span>{freeToPlay ? "Free to play" : "Paid game"}</span>
                    </div>
                    <h1>{game.title}</h1>
                    <p>{game.description || "Compare every available edition and store."}</p>
                    <div className="detail-price">
                        <span>{freeToPlay ? "Base game" : "Best current price"}</span>
                        <strong>{freeToPlay ? "Free to play" : currency(comparison?.lowestPrice)}</strong>
                        <small>
                            {freeToPlay
                                ? "Marketplace items are shown separately below."
                                : `${comparison?.offerCount || 0} verified base game offers`}
                        </small>
                    </div>
                </div>
            </section>

            <section className="offers-section">
                <div className="offers-toolbar">
                    <div>
                        <span className="section-kicker">PRICE COMPARISON</span>
                        <h2>{freeToPlay ? "Base game availability" : "Compare base game offers"}</h2>
                    </div>
                    {hasPlatformChoice && <div className="platform-control" aria-label="Platform filter">
                        <button
                            className={platform === "ALL" ? "selected" : ""}
                            onClick={() => setPlatform("ALL")}
                        >
                            All
                        </button>
                        {platforms.map((item) => {
                            const Icon = platformIcons[item] || Gamepad2;
                            return (
                                <button
                                    key={item}
                                    className={platform === item ? "selected" : ""}
                                    onClick={() => setPlatform(item)}
                                >
                                    <Icon size={16} />
                                    {item}
                                </button>
                            );
                        })}
                    </div>}
                </div>

                {loading ? (
                    <LoadingState label="Checking store prices..." />
                ) : baseGameOffers.length === 0 ? (
                    <EmptyState
                        title={freeToPlay ? "The base game is free" : "No matching offers"}
                        description={freeToPlay
                            ? "This title has no paid base-game offer. Its currencies and items are listed separately."
                            : "This game is in the catalog, but no verified store offer matches the selected platform."}
                    />
                ) : (
                    <div className="offer-list">
                        <div className="offer-list-header">
                            <span>Store</span>
                            <span>Edition</span>
                            <span>Delivery</span>
                            <span>Price</span>
                            <span />
                        </div>
                        {baseGameOffers.map((offer, index) => (
                            <OfferRow key={offer.productId} offer={offer} best={index === 0} />
                        ))}
                    </div>
                )}

                {marketplaceOffers.length > 0 && (
                    <section className="related-marketplace-section">
                        <div>
                            <span className="section-kicker">MARKETPLACE</span>
                            <h2>In-game products</h2>
                            <p>These products are not used to calculate the price of the base game.</p>
                        </div>
                        <div className="offer-list">
                            <div className="offer-list-header">
                                <span>Store</span>
                                <span>Product</span>
                                <span>Delivery</span>
                                <span>Price</span>
                                <span />
                            </div>
                            {marketplaceOffers.map((offer) => (
                                <OfferRow key={offer.productId} offer={offer} best={false} />
                            ))}
                        </div>
                    </section>
                )}
            </section>
        </div>
    );
}

function OfferRow({ offer, best }) {
    const savings =
        offer.price && offer.finalPrice && offer.price > offer.finalPrice
            ? Math.round((1 - offer.finalPrice / offer.price) * 100)
            : 0;

    return (
        <article className={best ? "offer-row best-offer" : "offer-row"}>
            <div className="store-cell">
        <span className="store-logo">
          <Store size={19} />
        </span>
                <div>
                    <strong>{offer.storeName}</strong>
                    <small>
                        {offer.officialStore ? (
                            <>
                                <Check size={12} /> Official store
                            </>
                        ) : (
                            "Marketplace seller"
                        )}
                    </small>
                </div>
            </div>
            <div className="edition-cell">
                <strong>{offer.productName}</strong>
                <small>
                    {offer.platform} {" / "} {offer.region || "GLOBAL"}
                </small>
            </div>
            <div className="delivery-cell">
                <span>{offer.deliveryType?.replaceAll("_", " ")}</span>
                <small>{offer.keyProvider || "Direct delivery"}</small>
            </div>
            <div className="offer-price">
                {savings > 0 && <span className="saving">-{savings}%</span>}
                {offer.discountPrice && <del>{currency(offer.price, offer.currency)}</del>}
                <strong>{currency(offer.finalPrice, offer.currency)}</strong>
            </div>
            {offer.storeUrl ? (
                <a
                    className="store-button"
                    href={offer.storeUrl}
                    target="_blank"
                    rel="noreferrer"
                >
                    Go to store
                    <ExternalLink size={15} />
                </a>
            ) : (
                <span className="store-button unavailable">Store link unavailable</span>
            )}
        </article>
    );
}

function ProductCatalog({ tab, products, loading, error, demo }) {
    const isTopup = tab === "topups";
    return (
        <section className="product-page">
            <div className="product-intro">
        <span className="eyebrow">
          {isTopup ? <Sparkles size={15} /> : <Tag size={15} />}
            {isTopup ? "Player account delivery" : "External market comparison"}
        </span>
                <h1>{isTopup ? "Top-up center" : "Item marketplace"}</h1>
                <p>
                    {isTopup
                        ? "Compare currency packs by amount, bonus and provider before you top up."
                        : "Inspect tradable items and continue safely to the selected external market."}
                </p>
            </div>

            <div className="section-heading compact">
                <div>
                    <span className="section-kicker">LIVE OFFERS</span>
                    <h2>{isTopup ? "Currency packs" : "Marketplace items"}</h2>
                </div>
                <span className="catalog-status">{products.length} results</span>
            </div>

            {demo && (
                <div className="demo-notice">
                    <Sparkles size={16} />
                    Demo products are shown because the API has no matching records yet.
                </div>
            )}

            {loading ? (
                <LoadingState label="Loading products..." />
            ) : error ? (
                <EmptyState title="Products unavailable" description={error} />
            ) : products.length === 0 ? (
                <EmptyState
                    title="Nothing matches yet"
                    description="Add products from the seller API and they will appear here."
                />
            ) : (
                <div className="product-grid">
                    {products.map((product, index) => (
                        <ProductCard
                            key={product.id}
                            product={product}
                            index={index}
                            topup={isTopup}
                        />
                    ))}
                </div>
            )}
        </section>
    );
}

function ProductCard({ product, index, topup }) {
    return (
        <article className="product-card">
            <div className="product-art">
                <img
                    src={resolveImage(product.imageUrl)}
                    alt=""
                    style={{ objectPosition: imagePosition(index + 1) }}
                    onError={(event) => {
                        event.currentTarget.src = FALLBACK_ART;
                    }}
                />
                <span className="product-type">
          {product.productType?.replaceAll("_", " ")}
        </span>
            </div>
            <div className="product-card-content">
                <div className="product-game">{product.gameTitle}</div>
                <h3>{product.name}</h3>
                {topup && (
                    <div className="amount-line">
                        <strong>
                            {(product.inGameAmount || 0) + (product.bonusAmount || 0)}
                        </strong>
                        <span>{product.inGameCurrencyName || "currency"}</span>
                        {product.bonusAmount > 0 && (
                            <em>+{product.bonusAmount} bonus</em>
                        )}
                    </div>
                )}
                <div className="product-footer">
                    <div>
                        <small>{product.storeName}</small>
                        <strong>{currency(product.finalPrice, product.currency)}</strong>
                    </div>
                    <a className="icon-button filled" href={product.storeUrl} target="_blank" rel="noreferrer" aria-label={`Open ${product.storeName}`}>
                        <ExternalLink size={17} />
                    </a>
                </div>
            </div>
        </article>
    );
}

function HeaderPopover({ type, authenticated, profile, wishlist, cart, loading, error, onRemoveWishlist, onRemoveCart, onBrowseGames, onBrowseMarketplace, onSelect }) {
    const cartTotal = cart.reduce((sum, item) => sum + Number(item.price || 0) * (item.quantity || 1), 0);
    const fullName = [profile?.firstName, profile?.lastName].filter(Boolean).join(" ") || profile?.username;

    if (!authenticated && type !== "profile") {
        return <div className="header-popover compact-popover"><PopoverGuest onBrowse={type === "wishlist" ? onBrowseGames : onBrowseMarketplace} /></div>;
    }

    if (type === "wishlist") {
        return <div className="header-popover" role="dialog" aria-label="Wishlist">
            <PopoverTitle icon={Heart} title="Wishlist" />
            {loading ? <PopoverLoading /> : error ? <PopoverError text={error} /> : wishlist.length === 0 ? <PopoverEmpty icon={Heart} title="Your wishlist is empty" copy="Save games and products you want to check later." action="Browse Games" onAction={onBrowseGames} /> : <>
                <div className="popover-items">{wishlist.map((product) => <WishlistRow key={product.id} product={product} onRemove={onRemoveWishlist} />)}</div>
                <button className="popover-link" onClick={onBrowseGames}>View Wishlist <ArrowRight size={15} /></button>
            </>}
        </div>;
    }

    if (type === "cart") {
        return <div className="header-popover" role="dialog" aria-label="Cart">
            <PopoverTitle icon={ShoppingBag} title="Your cart" />
            {loading ? <PopoverLoading /> : error ? <PopoverError text={error} /> : cart.length === 0 ? <PopoverEmpty icon={ShoppingBag} title="Your cart is empty" copy="Add a product when you are ready to buy." action="Browse Marketplace" onAction={onBrowseMarketplace} /> : <>
                <div className="popover-items">{cart.map((item) => <CartRow key={item.productId} item={item} onRemove={onRemoveCart} />)}</div>
                <div className="mini-cart-total"><span>Total</span><strong>{currency(cartTotal)}</strong></div>
                <div className="mini-cart-actions"><button className="secondary-button" onClick={onBrowseMarketplace}>View Cart</button><button className="primary-button" onClick={onBrowseMarketplace}>Checkout</button></div>
            </>}
        </div>;
    }

    return <div className="header-popover profile-popover" role="dialog" aria-label="Account menu">
        {!authenticated ? <PopoverGuest onBrowse={onBrowseGames} /> : <>
            <div className="account-popover-user"><span className="popover-avatar">{(fullName || "U").slice(0, 1).toUpperCase()}</span><div><strong>{fullName}</strong><small>{profile?.email}</small></div></div>
            <nav className="popover-account-nav"><button><CircleUserRound size={17} /> My profile</button><button><PackageCheck size={17} /> Orders</button><button onClick={() => onSelect("wishlist")}><Heart size={17} /> Wishlist</button><button onClick={() => onSelect("cart")}><ShoppingBag size={17} /> Cart</button><button><KeyRound size={17} /> Your keys</button></nav>
            <button className="popover-logout"><LogOut size={16} /> Logout</button>
        </>}
    </div>;
}

function PopoverTitle({ icon: Icon, title }) { return <div className="popover-title"><Icon size={18} /><strong>{title}</strong></div>; }
function PopoverLoading() { return <div className="popover-status"><LoaderCircle className="spinner" size={20} /> Loading…</div>; }
function PopoverError({ text }) { return <div className="popover-status error">{text}</div>; }
function PopoverEmpty({ icon: Icon, title, copy, action, onAction }) { return <div className="popover-empty"><Icon size={29} /><h3>{title}</h3><p>{copy}</p><button className="primary-button" onClick={onAction}>{action}<ArrowRight size={15} /></button></div>; }
function PopoverGuest({ onBrowse }) { return <div className="guest-popover"><h3>Welcome!</h3><p>Sign in to your account to access saved games, cart and orders.</p><button className="primary-button" onClick={onBrowse}><LogIn size={16} /> Sign In</button><button className="secondary-button" onClick={onBrowse}>Register</button><div className="guest-links"><span><Heart size={15} /> Wishlist</span><span><ShoppingBag size={15} /> Cart</span><span><PackageCheck size={15} /> Order history</span></div></div>; }
function WishlistRow({ product, onRemove }) { return <div className="popover-product"><img src={resolveImage(product.imageUrl)} alt="" onError={(event) => { event.currentTarget.src = FALLBACK_ART; }} /><div><strong>{product.name}</strong>{product.editionName && <small>{product.editionName}</small>}<span>{product.discountPrice && <del>{currency(product.price, product.currency)}</del>} <b>{currency(product.finalPrice ?? product.price, product.currency)}</b></span></div><button className="plain-icon" onClick={() => onRemove(product.id)} aria-label={`Remove ${product.name}`}><Trash2 size={16} /></button></div>; }
function CartRow({ item, onRemove }) { return <div className="popover-product"><span className="cart-item-mark"><ShoppingBag size={17} /></span><div><strong>{item.productName}</strong><small>Quantity: {item.quantity}</small><span><b>{currency(Number(item.price) * item.quantity)}</b></span></div><button className="plain-icon" onClick={() => onRemove(item.productId)} aria-label={`Remove ${item.productName}`}><Trash2 size={16} /></button></div>; }

function LoadingState({ label }) {
    return (
        <div className="state-panel">
            <LoaderCircle className="spinner" size={25} />
            <p>{label}</p>
        </div>
    );
}

function EmptyState({ title, description }) {
    return (
        <div className="state-panel">
            <Gamepad2 size={28} />
            <h3>{title}</h3>
            <p>{description}</p>
        </div>
    );
}

export default App;
