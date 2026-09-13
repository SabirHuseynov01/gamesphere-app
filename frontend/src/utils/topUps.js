const TIER_COUNT = 5;
const amountFormatter = new Intl.NumberFormat("en-US");

function normalizeLabel(value) {
    return (value || "").toLowerCase().replace(/[^a-z0-9]+/g, "");
}

export function toSlug(value) {
    return (value || "")
        .toLowerCase()
        .trim()
        .replace(/[^a-z0-9]+/g, "-")
        .replace(/(^-|-$)/g, "");
}

/**
 * Currency label of a package ("CP", "UC", "Gems").
 *
 * Catalogue rows whose inGameCurrencyName was filled with the game title are
 * treated as having no currency, otherwise every package renders as
 * "2400 Call of Duty Warzone" under a heading that already says the game.
 */
export function packageCurrencyName(product) {
    const currency = (product.inGameCurrencyName || "").trim();

    if (!currency || normalizeLabel(currency) === normalizeLabel(product.gameTitle)) {
        return "";
    }

    return currency;
}

/** Headline of a single package: "2400 CP", "Gold Pass", "1720 Coins". */
export function packageDenomination(product) {
    const currency = packageCurrencyName(product);

    if (product.inGameAmount) {
        const amount = amountFormatter.format(product.inGameAmount);
        return currency ? `${amount} ${currency}` : amount;
    }

    return product.editionName || product.name || currency || "";
}

/** Compact form used on catalogue cards: "300+40 UC". */
export function packageSummary(product) {
    const currency = packageCurrencyName(product);

    if (product.inGameAmount) {
        const amount = amountFormatter.format(product.inGameAmount);
        const bonus = product.bonusAmount > 0 ? `+${amountFormatter.format(product.bonusAmount)}` : "";
        return `${amount}${bonus}${currency ? ` ${currency}` : ""}`;
    }

    return packageDenomination(product);
}

/** Cheapest package of a game, used for the "From ..." price. */
export function cheapestProduct(products) {
    return products.reduce((cheapest, product) => {
        const price = Number(product.finalPrice);

        if (!Number.isFinite(price)) return cheapest;
        if (!cheapest) return product;

        return price < Number(cheapest.finalPrice) ? product : cheapest;
    }, null) || products[0] || null;
}

export function sortByPrice(products) {
    return [...products].sort((a, b) => Number(a.finalPrice ?? 0) - Number(b.finalPrice ?? 0));
}

/**
 * Visual weight of a package within its game, 1 (smallest) to TIER_COUNT.
 * Drives the denomination tile gradient so a package list reads as a ladder.
 */
export function packageTier(index, total) {
    if (total <= 1) return 1;

    return Math.min(TIER_COUNT, Math.floor((index / (total - 1)) * (TIER_COUNT - 1)) + 1);
}
