export function formatCurrency(amount, currency = "USD", unavailableLabel = "Price unavailable") {
    if (amount === null || amount === undefined) {
        return unavailableLabel;
    }

    const numericAmount = Number(amount);

    if (!Number.isFinite(numericAmount)) {
        return unavailableLabel;
    }

    try {
        return new Intl.NumberFormat("en-US", {
            style: "currency",
            currency,
        }).format(numericAmount);
    } catch {
        return `${numericAmount.toFixed(2)} ${currency}`;
    }
}

/**
 * Money whose currency may be unknown. Printing a bare number beats printing
 * the wrong symbol, which is what a hardcoded USD fallback would do.
 */
export function formatMoney(amount, currency, unavailableLabel = "Price unavailable") {
    if (currency) {
        return formatCurrency(amount, currency, unavailableLabel);
    }

    const numericAmount = Number(amount);

    return Number.isFinite(numericAmount) ? numericAmount.toFixed(2) : unavailableLabel;
}

export function formatEnum(value) {
    if (!value) {
        return "Not specified";
    }

    return value
        .toLowerCase()
        .split("_")
        .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
        .join(" ");
}

export function formatDateTime(value) {
    if (!value) {
        return "Not checked yet";
    }

    return new Intl.DateTimeFormat("en-GB", {
        dateStyle: "medium",
        timeStyle: "short",
    }).format(new Date(value));
}

export function formatPlatform(value) {
    const labels = {
        PC: "PC",
        PLAYSTATION: "PlayStation",
        XBOX: "Xbox",
        NINTENDO: "Nintendo",
        MOBILE: "Mobile",
    };

    return labels[value] || formatEnum(value);
}
