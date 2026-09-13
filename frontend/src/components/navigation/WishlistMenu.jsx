import { Heart, Trash2 } from "lucide-react";
import { Link } from "react-router-dom";

import HeaderMenu from "./HeaderMenu.jsx";
import { useAccount } from "../../account/AccountProvider.jsx";
import { useTranslation } from "../../i18n/index.jsx";
import { formatCurrency } from "../../utils/formatters.js";

export default function WishlistMenu() {
    const { t } = useTranslation();
    const { isAuthenticated, toggleWishlist, wishlist, wishlistCount } = useAccount();

    return (
        <HeaderMenu
            badge={wishlistCount}
            icon={<Heart size={19} />}
            label={t("nav.wishlist")}
            panelClassName="basket-menu"
        >
            {({ close }) => (
                <>
                    <header className="basket-menu__header">
                        <h2>{t("nav.wishlist")}</h2>
                        <span>{t("wishlist.count", { count: wishlistCount })}</span>
                    </header>

                    {!isAuthenticated ? (
                        <p className="basket-menu__empty">{t("wishlist.signInFirst")}</p>
                    ) : wishlist.length === 0 ? (
                        <p className="basket-menu__empty">{t("wishlist.empty")}</p>
                    ) : (
                        <>
                            <ul className="basket-menu__list">
                                {wishlist.map((product) => (
                                    <li key={product.id}>
                                        <span className="basket-menu__name">
                                            {product.name}
                                            {product.gameTitle && <small>{product.gameTitle}</small>}
                                        </span>
                                        <span className="basket-menu__meta">
                                            <strong>{formatCurrency(product.finalPrice, product.currency || "USD")}</strong>
                                        </span>
                                        <button
                                            aria-label={t("wishlist.remove")}
                                            title={t("wishlist.remove")}
                                            type="button"
                                            onClick={() => toggleWishlist(product.id)}
                                        >
                                            <Trash2 size={15} />
                                        </button>
                                    </li>
                                ))}
                            </ul>

                            <footer className="basket-menu__footer">
                                <Link to="/account/wishlist" onClick={close}>{t("wishlist.viewAll")}</Link>
                            </footer>
                        </>
                    )}
                </>
            )}
        </HeaderMenu>
    );
}
