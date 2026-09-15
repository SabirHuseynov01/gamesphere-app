import { createContext, useContext, useMemo, useState } from "react";

const STORAGE_KEY = "gamesphere-display-currency";

/**
 * Rates against USD. A product is stored with its own currency; the display
 * currency is a presentation layer on top, never what gets charged — Stripe
 * settles in the currency the order actually carries.
 */
const RATE_PER_USD = {
    USD: 1,
    AZN: Number(import.meta.env.VITE_AZN_PER_USD) || 1.7,
};

const DISPLAY_CURRENCIES = Object.keys(RATE_PER_USD);

const SYMBOL = { USD: "$", AZN: "₼" };

const CurrencyContext = createContext(null);

function readStored() {
    try {
        const stored = window.localStorage.getItem(STORAGE_KEY);
        return RATE_PER_USD[stored] ? stored : "AZN";
    } catch {
        return "AZN";
    }
}

function render(amount, currency) {
    const symbol = SYMBOL[currency];
    const value = amount.toFixed(2);

    return symbol ? `${symbol}${value}` : `${value} ${currency}`;
}

export function CurrencyProvider({ children }) {
    const [displayCurrency, setDisplayCurrencyState] = useState(readStored);

    function setDisplayCurrency(next) {
        const safe = RATE_PER_USD[next] ? next : "AZN";
        setDisplayCurrencyState(safe);
        try {
            window.localStorage.setItem(STORAGE_KEY, safe);
        } catch {
            // A browser refusing storage still gets a working switch for this visit.
        }
    }

    const value = useMemo(() => ({
        displayCurrency,
        setDisplayCurrency,
        currencies: DISPLAY_CURRENCIES,

        /**
         * Format an amount for display, converting into the selected currency
         * when a rate exists. A currency with no known rate is shown as it is
         * rather than converted at a made-up rate.
         */
        formatPrice(amount, sourceCurrency) {
            const numeric = Number(amount);
            if (!Number.isFinite(numeric)) return "—";

            const from = RATE_PER_USD[sourceCurrency];
            const to = RATE_PER_USD[displayCurrency];

            if (!from || !to || sourceCurrency === displayCurrency) {
                return render(numeric, sourceCurrency || displayCurrency);
            }

            return render((numeric / from) * to, displayCurrency);
        },

        /** True when the value shown is a conversion rather than the stored price. */
        isConverted(sourceCurrency) {
            return Boolean(
                sourceCurrency
                && RATE_PER_USD[sourceCurrency]
                && sourceCurrency !== displayCurrency,
            );
        },

        rateFor(sourceCurrency) {
            const from = RATE_PER_USD[sourceCurrency];
            const to = RATE_PER_USD[displayCurrency];
            return from && to ? to / from : null;
        },
    }), [displayCurrency]);

    return <CurrencyContext.Provider value={value}>{children}</CurrencyContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useMoney() {
    const context = useContext(CurrencyContext);
    if (!context) throw new Error("useMoney must be used inside CurrencyProvider");
    return context;
}
