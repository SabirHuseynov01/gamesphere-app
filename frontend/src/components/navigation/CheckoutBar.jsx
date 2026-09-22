import { ArrowRight, ShoppingCart } from "lucide-react";
import { Link, useLocation } from "react-router-dom";

import { useAccount } from "../../account/AccountProvider.jsx";
import { useTranslation } from "../../i18n/index.jsx";
import { useMoney } from "../../money/CurrencyProvider.jsx";

/**
 * A standing route to payment. Without it the only way to the checkout is the
 * cart dropdown, which a shopper has to know to open — so a filled cart looked
 * like a dead end.
 */
export default function CheckoutBar() {
    const { pathname } = useLocation();
    const { cart, cartCount, isAuthenticated } = useAccount();
    const { formatPrice } = useMoney();
    const { t } = useTranslation();

    // Nothing to nudge towards on the pages that already are the payment flow.
    const onPaymentRoute = pathname.startsWith("/checkout") || pathname.startsWith("/payment");

    if (!isAuthenticated || cartCount === 0 || onPaymentRoute) return null;

    return (
        <div className="checkout-bar">
            <div className="container checkout-bar__inner">
                <span className="checkout-bar__summary">
                    <ShoppingCart size={18} />
                    <strong>{t("cart.count", { count: cartCount })}</strong>
                    <em>{formatPrice(cart.totalAmount, cart.currency)}</em>
                </span>

                <Link className="checkout-bar__action" to="/checkout">
                    {t("cart.checkout")}
                    <ArrowRight size={17} />
                </Link>
            </div>
        </div>
    );
}
