import {
    BadgeCheck,
    ExternalLink,
    PackageCheck,
    Store,
} from "lucide-react";

import {
    formatCurrency,
    formatDateTime,
    formatEnum,
    formatPlatform,
} from "../../../utils/formatters.js";
import { getStoreLogo } from "../../../utils/storeLogos.js";
import { useTranslation } from "../../../i18n/index.jsx";

export default function OfferCard({ offer, bestPrice = false }) {
    const { t } = useTranslation();
    const finalPrice =
        offer.finalPrice ?? offer.discountPrice ?? offer.price;

    const canVisitStore = Boolean(offer.storeUrl);
    const storeLogo = getStoreLogo(offer.storeName);

    return (
        <article className="offer-card">
            <div className="offer-card__store">
                <div className="offer-card__store-icon">
                    {storeLogo ? (
                        <img src={storeLogo} alt={`${offer.storeName || "Store"} logo`} />
                    ) : (
                        <Store size={20} />
                    )}
                </div>

                <div>
                    <div className="offer-card__store-name">
                        <strong>{offer.storeName || t("common.unknown")}</strong>

                        {offer.officialStore && (
                            <span title={t("offers.officialStore")}>
                <BadgeCheck size={17} />
              </span>
                        )}
                    </div>

                    <span>{offer.region || t("common.global")} · {offer.currency || "USD"}</span>
                </div>
            </div>

            <div className="offer-card__product">
                <strong>{offer.productName}</strong>
                <span>
          {formatPlatform(offer.platform)} · {formatEnum(offer.productType)}
        </span>

                {offer.inGameAmount !== null &&
                    offer.inGameAmount !== undefined && (
                        <span>
              {offer.inGameAmount} {offer.inGameCurrencyName || t("offers.credits")}
                            {offer.bonusAmount ? ` + ${offer.bonusAmount} ${t("offers.bonus")}` : ""}
            </span>
                    )}
            </div>

            <div className="offer-card__delivery">
                <PackageCheck size={18} />
                <span>{formatEnum(offer.deliveryType)}</span>
            </div>

            <div className="offer-card__price">
                {offer.discountPrice && offer.price !== offer.discountPrice && (
                    <del>{formatCurrency(offer.price, offer.currency, t("common.unknown"))}</del>
                )}

                <strong>{formatCurrency(finalPrice, offer.currency, t("common.unknown"))}</strong>
                {bestPrice && <span className="offer-card__best-price">{t("offers.bestPrice")}</span>}
                <span>{t("offers.checked")}: {formatDateTime(offer.lastCheckedAt)}</span>
            </div>

            {canVisitStore ? (
                <a
                    className="offer-card__visit"
                    href={offer.storeUrl}
                    target="_blank"
                    rel="noreferrer"
                >
                    {t("offers.visitStore")}
                    <ExternalLink size={16} />
                </a>
            ) : (
                <span className="offer-card__visit offer-card__visit--disabled">
          {t("offers.unavailable")}
        </span>
            )}
        </article>
    );
}
