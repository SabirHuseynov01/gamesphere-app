import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";

import * as accountApi from "../api/accountApi.js";
import { getApiErrorMessage } from "../api/httpClient.js";

const ACCESS_TOKEN_KEY = "accessToken";
const REFRESH_TOKEN_KEY = "gamesphere-refresh-token";
const USER_KEY = "gamesphere-user";

const AccountContext = createContext(null);

function readStoredUser() {
    try {
        const raw = window.localStorage.getItem(USER_KEY);
        return raw ? JSON.parse(raw) : null;
    } catch {
        return null;
    }
}

function storeSession(auth) {
    const user = { username: auth.username, email: auth.email };

    window.localStorage.setItem(ACCESS_TOKEN_KEY, auth.accessToken);
    window.localStorage.setItem(REFRESH_TOKEN_KEY, auth.refreshToken || "");
    window.localStorage.setItem(USER_KEY, JSON.stringify(user));

    return user;
}

function clearSession() {
    window.localStorage.removeItem(ACCESS_TOKEN_KEY);
    window.localStorage.removeItem(REFRESH_TOKEN_KEY);
    window.localStorage.removeItem(USER_KEY);
}

const emptyCart = { items: [], totalAmount: 0, currency: null };

function toCart(response) {
    return {
        items: response?.items || [],
        totalAmount: response?.totalAmount ?? 0,
        currency: response?.currency ?? null,
    };
}

export function AccountProvider({ children }) {
    const [user, setUser] = useState(readStoredUser);
    const [cart, setCart] = useState(emptyCart);
    const [wishlist, setWishlist] = useState([]);
    // A signed-in visitor starts with empty baskets that are not yet loaded;
    // pages must be able to tell that apart from baskets that are really empty.
    const [basketsLoading, setBasketsLoading] = useState(() => Boolean(readStoredUser()));

    const signOut = useCallback(async () => {
        const refreshToken = window.localStorage.getItem(REFRESH_TOKEN_KEY);

        try {
            await accountApi.logout(refreshToken);
        } catch {
            // The session is being discarded either way; a failed revoke must not
            // leave the user stuck in a signed-in shell.
        }

        clearSession();
        setUser(null);
        setCart(emptyCart);
        setWishlist([]);
        setBasketsLoading(false);
    }, []);

    const refreshBaskets = useCallback(async (signal) => {
        const [cartResult, wishlistResult] = await Promise.allSettled([
            accountApi.getCart(signal),
            accountApi.getWishlist(signal),
        ]);

        if (signal?.aborted) return;

        setBasketsLoading(false);

        if (cartResult.status === "fulfilled") {
            setCart(toCart(cartResult.value));
        }

        if (wishlistResult.status === "fulfilled") {
            setWishlist(wishlistResult.value?.products || []);
        }

        // A stored token that the backend no longer accepts means the session is
        // gone; drop it rather than rendering a signed-in header that cannot load.
        const rejected = [cartResult, wishlistResult].find((result) => result.status === "rejected");
        if (rejected?.reason?.response?.status === 401) {
            clearSession();
            setUser(null);
            setCart(emptyCart);
            setWishlist([]);
        }
    }, []);

    useEffect(() => {
        if (!user) return undefined;

        const controller = new AbortController();
        // Deferred so the state updates land after the effect returns rather
        // than cascading a second render out of this one.
        const load = Promise.resolve().then(() => refreshBaskets(controller.signal));

        return () => {
            controller.abort();
            load.catch(() => {});
        };
    }, [refreshBaskets, user]);

    const signIn = useCallback(async (email, password) => {
        const session = storeSession(await accountApi.login(email, password));
        setBasketsLoading(true);
        setUser(session);
    }, []);

    const createAccount = useCallback(async (payload) => {
        const session = storeSession(await accountApi.register(payload));
        setBasketsLoading(true);
        setUser(session);
    }, []);

    const addToCart = useCallback(async (productId, playerAccountId) => {
        setCart(toCart(await accountApi.addCartItem(productId, 1, playerAccountId)));
    }, []);

    const removeFromCart = useCallback(async (productId) => {
        setCart(toCart(await accountApi.removeCartItem(productId)));
    }, []);

    const toggleWishlist = useCallback(async (productId) => {
        const inWishlist = wishlist.some((product) => product.id === productId);
        const updated = inWishlist
            ? await accountApi.removeWishlistItem(productId)
            : await accountApi.addWishlistItem(productId);

        setWishlist(updated?.products || []);
    }, [wishlist]);

    const value = useMemo(() => ({
        user,
        isAuthenticated: Boolean(user),
        basketsLoading,
        cart,
        cartCount: cart.items.reduce((total, item) => total + (item.quantity || 0), 0),
        wishlist,
        wishlistCount: wishlist.length,
        signIn,
        createAccount,
        signOut,
        addToCart,
        removeFromCart,
        toggleWishlist,
        getApiErrorMessage,
    }), [addToCart, basketsLoading, cart, createAccount, removeFromCart, signIn, signOut, toggleWishlist, user, wishlist]);

    return <AccountContext.Provider value={value}>{children}</AccountContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAccount() {
    const context = useContext(AccountContext);
    if (!context) throw new Error("useAccount must be used inside AccountProvider");
    return context;
}
