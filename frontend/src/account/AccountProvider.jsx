import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";

import * as accountApi from "../api/accountApi.js";
import { getApiErrorMessage } from "../api/httpClient.js";
import {
    clearSession,
    readRefreshToken,
    readStoredUser,
    SESSION_EXPIRED_EVENT,
    storeSession,
} from "../api/session.js";

const AccountContext = createContext(null);

const emptyCart = { items: [], totalAmount: 0, currency: null };

const ADMIN_ROLE = "ROLE_ADMIN";

function toCart(response) {
    return {
        items: response?.items || [],
        totalAmount: response?.totalAmount ?? 0,
        currency: response?.currency ?? null,
    };
}

export function AccountProvider({ children }) {
    const [user, setUser] = useState(readStoredUser);
    // The header decides which panels to offer from the profile's roles, so the
    // profile is loaded once here instead of in every page that needs it.
    const [profile, setProfile] = useState(null);
    const [cart, setCart] = useState(emptyCart);
    const [wishlist, setWishlist] = useState([]);
    // A signed-in visitor starts with empty baskets that are not yet loaded;
    // pages must be able to tell that apart from baskets that are really empty.
    const [basketsLoading, setBasketsLoading] = useState(() => Boolean(readStoredUser()));

    const signOut = useCallback(async () => {
        const refreshToken = readRefreshToken();

        try {
            await accountApi.logout(refreshToken);
        } catch {
            // The session is being discarded either way; a failed revoke must not
            // leave the user stuck in a signed-in shell.
        }

        clearSession();
        setUser(null);
        setProfile(null);
        setCart(emptyCart);
        setWishlist([]);
        setBasketsLoading(false);
    }, []);

    const refreshBaskets = useCallback(async (signal) => {
        const [cartResult, wishlistResult, profileResult] = await Promise.allSettled([
            accountApi.getCart(signal),
            accountApi.getWishlist(signal),
            accountApi.getProfile(signal),
        ]);

        if (signal?.aborted) return;

        setBasketsLoading(false);

        if (cartResult.status === "fulfilled") {
            setCart(toCart(cartResult.value));
        }

        if (wishlistResult.status === "fulfilled") {
            setWishlist(wishlistResult.value?.products || []);
        }

        if (profileResult.status === "fulfilled") {
            setProfile(profileResult.value);
        }

        // The client already retried these with a refreshed token, so a 401 that
        // still lands here means the refresh failed too: the session is over.
        const rejected = [cartResult, wishlistResult].find((result) => result.status === "rejected");
        if (rejected?.reason?.response?.status === 401) {
            clearSession();
            setUser(null);
            setProfile(null);
            setCart(emptyCart);
            setWishlist([]);
        }
    }, []);

    // An access token lives 15 minutes. The client renews it silently, but once
    // the refresh token is rejected too the header must stop claiming a session.
    useEffect(() => {
        function handleExpiry() {
            setUser(null);
            setProfile(null);
            setCart(emptyCart);
            setWishlist([]);
            setBasketsLoading(false);
        }

        window.addEventListener(SESSION_EXPIRED_EVENT, handleExpiry);
        return () => window.removeEventListener(SESSION_EXPIRED_EVENT, handleExpiry);
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

    const setCartPlayerId = useCallback(async (productId, playerAccountId) => {
        setCart(toCart(await accountApi.setCartItemPlayerId(productId, playerAccountId)));
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
        profile,
        setProfile,
        roles: profile?.roles || [],
        // UI gating only. Every admin endpoint is guarded by @PreAuthorize, so
        // flipping this in the browser opens nothing.
        isAdmin: Boolean(profile?.roles?.includes(ADMIN_ROLE)),
        isSeller: Boolean(profile?.seller ?? profile?.isSeller),
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
        setCartPlayerId,
        toggleWishlist,
        getApiErrorMessage,
    }), [addToCart, basketsLoading, cart, createAccount, profile, removeFromCart, setCartPlayerId, signIn, signOut, toggleWishlist, user, wishlist]);

    return <AccountContext.Provider value={value}>{children}</AccountContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAccount() {
    const context = useContext(AccountContext);
    if (!context) throw new Error("useAccount must be used inside AccountProvider");
    return context;
}
