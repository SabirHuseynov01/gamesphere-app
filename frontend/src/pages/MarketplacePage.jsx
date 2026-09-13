import { useEffect, useMemo, useState } from "react";
import { ExternalLink, Filter, Tag } from "lucide-react";

import { getCatalogProducts } from "../api/catalogApi.js";
import { searchGames } from "../api/gameApi.js";
import { getApiErrorMessage } from "../api/httpClient.js";
import { resolveMediaUrl } from "../utils/mediaUrl.js";
import { formatCurrency, formatEnum, formatPlatform } from "../utils/formatters.js";
import { useTranslation } from "../i18n/index.jsx";
import "../styles/top-ups.css";

const productTypeOptions = [
    ["", "All"],
    ["GAME", "Game"],
    ["DLC", "DLC"],
    ["EXPANSION", "Expansion"],
    ["ADD_ON", "Add-on"],
    ["ITEM", "Item"],
    ["CURRENCY", "Currency"],
];

const sortOptions = [
    ["DEFAULT", "Recommended"],
    ["PRICE_ASC", "Price: Low to High"],
    ["PRICE_DESC", "Price: High to Low"],
    ["NAME_ASC", "Alphabetical"],
];

function matchesProductType(product, selectedType) {
    if (!selectedType) return true;
    if (selectedType === "EXPANSION") return product.productType === "BUNDLE";
    if (selectedType === "ADD_ON") return product.productType === "BATTLE_PASS";
    if (selectedType === "ITEM") return product.productType === "IN_GAME_ITEM";
    if (selectedType === "CURRENCY") return product.productType === "IN_GAME_CURRENCY";
    return product.productType === selectedType;
}

export default function MarketplacePage() {
    const { t } = useTranslation();
    const [products, setProducts] = useState([]);
    const [games, setGames] = useState([]);
    const [filters, setFilters] = useState({ productType: "", platform: "", store: "", edition: "", sort: "DEFAULT" });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const controller = new AbortController();

        Promise.allSettled([
            getCatalogProducts("MARKETPLACE", controller.signal),
            searchGames({ page: 0, size: 100, signal: controller.signal }),
        ])
            .then(([catalogResult, gameResult]) => {
                if (catalogResult.status === "fulfilled") setProducts(catalogResult.value || []);
                if (gameResult.status === "fulfilled") {
                    const gamePage = gameResult.value;
                    setGames(gamePage?.content || gamePage || []);
                }
                if (catalogResult.status === "rejected") throw catalogResult.reason;
            })
            .catch((requestError) => {
                if (requestError.name !== "CanceledError") {
                    setError(getApiErrorMessage(requestError));
                }
            })
            .finally(() => {
                if (!controller.signal.aborted) setLoading(false);
            });

        return () => controller.abort();
    }, []);

    const gameCovers = useMemo(() => new Map(
        games.map((game) => [String(game.id), game.coverImageUrl]),
    ), [games]);

    const filteredProducts = useMemo(() => {
        const result = products.filter((product) => {
            const finalPrice = Number(product.finalPrice ?? product.discountPrice ?? product.price);
            return matchesProductType(product, filters.productType)
                && (!filters.platform || product.platform === filters.platform)
                && (!filters.store || product.storeName === filters.store)
                && (!filters.edition || (product.editionName || "").toLowerCase().includes(filters.edition.toLowerCase()))
                && (Number.isNaN(finalPrice) || finalPrice >= 0);
        });

        return result.sort((left, right) => {
            if (filters.sort === "PRICE_ASC" || filters.sort === "PRICE_DESC") {
                const leftPrice = Number(left.finalPrice ?? left.discountPrice ?? left.price);
                const rightPrice = Number(right.finalPrice ?? right.discountPrice ?? right.price);
                return filters.sort === "PRICE_ASC" ? leftPrice - rightPrice : rightPrice - leftPrice;
            }
            if (filters.sort === "NAME_ASC") return (left.name || "").localeCompare(right.name || "");
            return 0;
        });
    }, [filters, products]);

    const platforms = [...new Set(products.map((product) => product.platform).filter(Boolean))];
    const stores = [...new Set(products.map((product) => product.storeName).filter(Boolean))];

    return (
        <div className="container catalog-products-page">
            <header className="top-ups-header">
                <div>
                    <p className="section-kicker">{t("marketplace.eyebrow")}</p>
                    <h1>{t("marketplace.title")}</h1>
                    <p>{t("marketplace.description")}</p>
                </div>
                <strong>{t("marketplace.products", { count: filteredProducts.length })}</strong>
            </header>

            <section className="marketplace-filters" aria-label="Marketplace filters">
                <div className="marketplace-filters__title"><Filter size={17} /> Filters</div>
                <label>Product type<select value={filters.productType} onChange={(event) => setFilters({ ...filters, productType: event.target.value })}>
                    {productTypeOptions.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
                </select></label>
                <label>Platform<select value={filters.platform} onChange={(event) => setFilters({ ...filters, platform: event.target.value })}>
                    <option value="">All platforms</option>
                    {platforms.map((platform) => <option key={platform} value={platform}>{formatPlatform(platform)}</option>)}
                </select></label>
                <label>Store<select value={filters.store} onChange={(event) => setFilters({ ...filters, store: event.target.value })}>
                    <option value="">All stores</option>
                    {stores.map((store) => <option key={store} value={store}>{store}</option>)}
                </select></label>
                <label>Edition<input value={filters.edition} onChange={(event) => setFilters({ ...filters, edition: event.target.value })} placeholder="Standard, Deluxe..." /></label>
                <label>Sort<select value={filters.sort} onChange={(event) => setFilters({ ...filters, sort: event.target.value })}>
                    {sortOptions.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
                </select></label>
                <button type="button" className="filter-reset" onClick={() => setFilters({ productType: "", platform: "", store: "", edition: "", sort: "DEFAULT" })}>{t("common.reset")}</button>
            </section>

            {loading && <div className="status-panel">{t("marketplace.loading")}</div>}
            {!loading && error && <div className="status-panel status-panel--error">{error}</div>}
            {!loading && !error && filteredProducts.length === 0 && (
                <div className="top-ups-empty">
                    <Tag size={32} />
                    <h2>{t("marketplace.emptyTitle")}</h2>
                    <p>{t("marketplace.emptyText")}</p>
                </div>
            )}

            {!loading && !error && filteredProducts.length > 0 && (
                <section className="catalog-product-grid">
                    {filteredProducts.map((product) => {
                        const imageUrl = resolveMediaUrl(product.imageUrl || gameCovers.get(String(product.gameId)));

                        return (
                            <article className="catalog-product-card" key={product.id}>
                                <div className="catalog-product-card__image">
                                    {imageUrl ? <img src={imageUrl} alt={`${product.name} cover`} /> : <Tag size={38} />}
                                </div>
                                <div className="catalog-product-card__body">
                                    <small>{formatEnum(product.productType)} · {product.editionName || "Edition"}</small>
                                    <h2>{product.name}</h2>
                                    <p>{product.region || "GLOBAL"} · {product.storeName}</p>
                                    <strong>{formatCurrency(product.finalPrice ?? product.discountPrice ?? product.price, product.currency || "USD")}</strong>
                                    <a href={product.storeUrl} target="_blank" rel="noreferrer">
                                        {t("common.visitStore")} <ExternalLink size={16} />
                                    </a>
                                </div>
                            </article>
                        );
                    })}
                </section>
            )}
        </div>
    );
}
