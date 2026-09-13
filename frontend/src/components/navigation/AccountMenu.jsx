import { useState } from "react";
import { Heart, LayoutDashboard, LogOut, Receipt, UserRound } from "lucide-react";
import { Link } from "react-router-dom";

import HeaderMenu from "./HeaderMenu.jsx";
import { useAccount } from "../../account/AccountProvider.jsx";
import { useTranslation } from "../../i18n/index.jsx";

function AccountLinks({ close, t }) {
    return (
        <nav className="account-menu__links" aria-label={t("account.menu")}>
            <Link to="/account" onClick={close}>
                <LayoutDashboard size={16} />
                {t("account.dashboard")}
            </Link>
            <Link to="/account/orders" onClick={close}>
                <Receipt size={16} />
                {t("account.orders")}
            </Link>
            <Link to="/account/wishlist" onClick={close}>
                <Heart size={16} />
                {t("nav.wishlist")}
            </Link>
        </nav>
    );
}

function AuthForm({ close }) {
    const { t } = useTranslation();
    const { signIn, createAccount, getApiErrorMessage } = useAccount();
    const [mode, setMode] = useState("SIGN_IN");
    const [form, setForm] = useState({ username: "", email: "", password: "", firstName: "", lastName: "" });
    const [error, setError] = useState("");
    const [busy, setBusy] = useState(false);

    const registering = mode === "REGISTER";

    function update(field) {
        return (event) => setForm((current) => ({ ...current, [field]: event.target.value }));
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setBusy(true);
        setError("");

        try {
            if (registering) {
                await createAccount(form);
            } else {
                await signIn(form.email, form.password);
            }
            close();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError));
        } finally {
            setBusy(false);
        }
    }

    return (
        <form className="account-menu__form" onSubmit={handleSubmit}>
            <h2>{t("account.welcome")}</h2>
            <p>{registering ? t("account.registerIntro") : t("account.signInIntro")}</p>

            {registering && (
                <>
                    <label>
                        {t("account.username")}
                        <input required value={form.username} onChange={update("username")} />
                    </label>
                    <div className="account-menu__row">
                        <label>
                            {t("account.firstName")}
                            <input value={form.firstName} onChange={update("firstName")} />
                        </label>
                        <label>
                            {t("account.lastName")}
                            <input value={form.lastName} onChange={update("lastName")} />
                        </label>
                    </div>
                </>
            )}

            <label>
                {t("account.email")}
                <input autoComplete="email" required type="email" value={form.email} onChange={update("email")} />
            </label>

            <label>
                {t("account.password")}
                <input
                    autoComplete={registering ? "new-password" : "current-password"}
                    minLength={registering ? 6 : undefined}
                    required
                    type="password"
                    value={form.password}
                    onChange={update("password")}
                />
            </label>

            {error && <p className="account-menu__error">{error}</p>}

            <button className="account-menu__primary" disabled={busy} type="submit">
                {busy ? t("account.working") : registering ? t("account.register") : t("account.signIn")}
            </button>

            <p className="account-menu__switch">
                {registering ? t("account.haveAccount") : t("account.noAccount")}
                <button type="button" onClick={() => { setMode(registering ? "SIGN_IN" : "REGISTER"); setError(""); }}>
                    {registering ? t("account.signIn") : t("account.register")}
                </button>
            </p>
        </form>
    );
}

export default function AccountMenu() {
    const { t } = useTranslation();
    const { user, isAuthenticated, signOut } = useAccount();

    return (
        <HeaderMenu
            icon={<UserRound size={19} />}
            label={isAuthenticated ? user.username : t("nav.profile")}
            panelClassName="account-menu"
        >
            {({ close }) => (isAuthenticated ? (
                <>
                    <div className="account-menu__identity">
                        <span className="account-menu__avatar" aria-hidden="true">
                            {user.username.slice(0, 1).toUpperCase()}
                        </span>
                        <span>
                            <strong>{user.username}</strong>
                            <small>{user.email}</small>
                        </span>
                    </div>

                    <AccountLinks close={close} t={t} />

                    <button
                        className="account-menu__signout"
                        type="button"
                        onClick={() => { signOut(); close(); }}
                    >
                        <LogOut size={16} />
                        {t("account.signOut")}
                    </button>
                </>
            ) : (
                <AuthForm close={close} />
            ))}
        </HeaderMenu>
    );
}
