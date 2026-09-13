import OfferCard from "./OfferCard.jsx";
import { formatCurrency, formatEnum } from "../../../utils/formatters.js";
import { useTranslation } from "../../../i18n/index.jsx";

export default function OfferList({ comparison, title }) {
    const { t } = useTranslation();
    const offers = comparison.offers || [];
    const platforms = comparison.availablePlatforms || [];
    const groupedOffers = offers.reduce((groups, offer) => {
        const key = offer.editionName?.trim()
            || offer.productName
            || formatEnum(offer.productType)
            || t("common.products");
        if (!groups.has(key)) groups.set(key, []);
        groups.get(key).push(offer);
        return groups;
    }, new Map());

    function finalPrice(offer) {
        const value = offer.finalPrice ?? offer.discountPrice ?? offer.price;
        return value === null || value === undefined ? Number.POSITIVE_INFINITY : Number(value);
    }

    return (
        <section className="offers-section">
            <header className="offers-section__header">
                <div>
                    <h2>{title || t("offers.storeOffers")}</h2>
                    <p>{t("offers.foundAcross", { count: comparison.offerCount ?? offers.length, platforms: platforms.length })}</p>
                </div>

                {comparison.lowestPrice !== null &&
                    comparison.lowestPrice !== undefined && (
                        <div className="lowest-price">
                            <span>{t("common.lowestPrice")}</span>
                            <strong>{formatCurrency(comparison.lowestPrice, "USD", t("common.unknown"))}</strong>
                        </div>
                    )}
            </header>

            {platforms.length > 0 && (
                <div className="platform-badges">
                    {platforms.map((platform) => (
                        <span key={platform}>{formatEnum(platform)}</span>
                    ))}
                </div>
            )}

            {offers.length === 0 ? (
                <div className="offers-empty">
                {t("common.noOffers")}
                </div>
            ) : (
                <div className="offer-list">
                    {[...groupedOffers.entries()].map(([groupName, groupOffers]) => {
                        const lowest = Math.min(...groupOffers.map(finalPrice));
                        return <section className="offer-group" key={groupName}>
                            <h3>{groupName}</h3>
                            {groupOffers.map((offer) => <OfferCard key={offer.productId} offer={offer} bestPrice={finalPrice(offer) === lowest} />)}
                        </section>;
                    })}
                </div>
            )}
        </section>
    );
}
