import { ShoppingCart, Trash2 } from "lucide-react";
import { Link } from "react-router-dom";

import HeaderMenu from "./HeaderMenu.jsx";
import { useAccount } from "../../account/AccountProvider.jsx";
import { useTranslation } from "../../i18n/index.jsx";
import { useMoney } from "../../money/CurrencyProvider.jsx";

export default function CartMenu() {
    const { t } = useTranslation();
    const { cart, cartCount, isAuthenticated, removeFromCart } = useAccount();
    const { formatPrice } = useMoney();

    return (
        <HeaderMenu
            badge={cartCount}
            icon={<ShoppingCart size={19} />}
            label={t("nav.cart")}
            panelClassName="basket-menu"
        >
            {({ close }) => (
                <>
                    <header className="basket-menu__header">
                        <h2>{t("nav.cart")}</h2>
                        <span>{t("cart.count", { count: cartCount })}</span>
                    </header>

                    {!isAuthenticated ? (
                        <p className="basket-menu__empty">{t("cart.signInFirst")}</p>
                    ) : cart.items.length === 0 ? (
                        <p className="basket-menu__empty">{t("cart.empty")}</p>
                    ) : (
                        <>
                            <ul className="basket-menu__list">
                                {cart.items.map((item) => (
                                    <li key={item.productId}>
                                        <span className="basket-menu__name">
                                            {item.productName}
                                            {item.playerAccountId && <small>{item.playerAccountId}</small>}
                                        </span>
                                        <span className="basket-menu__meta">
                                            <strong>{formatPrice(item.price, item.currency || cart.currency)}</strong>
                                            <small>× {item.quantity}</small>
                                        </span>
                                        <button
                                            aria-label={t("cart.remove")}
                                            title={t("cart.remove")}
                                            type="button"
                                            onClick={() => removeFromCart(item.productId)}
                                        >
                                            <Trash2 size={15} />
                                        </button>
                                    </li>
                                ))}
                            </ul>

                            <footer className="basket-menu__footer">
                                <span>
                                    {t("cart.total")}
                                    <strong>{formatPrice(cart.totalAmount, cart.currency)}</strong>
                                </span>
                                <Link to="/checkout" onClick={close}>{t("cart.checkout")}</Link>
                            </footer>
                        </>
                    )}
                </>
            )}
        </HeaderMenu>
    );
}
