package com.example.gamesphere.specification;

import com.example.gamesphere.dto.request.GameOfferCriteria;
import com.example.gamesphere.dto.request.GameSearchCriteria;
import com.example.gamesphere.entity.Game;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.enums.GameBrowseSort;
import com.example.gamesphere.enums.GameGenre;
import com.example.gamesphere.enums.Platform;
import com.example.gamesphere.enums.ProductStatus;
import com.example.gamesphere.enums.ProductType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class GameSpecification {

    private GameSpecification() {
    }

    public static Specification<Game> withSearchCriteria(GameSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));

            if (criteria == null) {
                applyGameSorting(root, query, cb, GameBrowseSort.ALPHABETICAL);
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            addTextPredicates(root, cb, criteria, predicates);

            if (criteria.getReleaseDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("releaseDate"), criteria.getReleaseDateFrom()));
            }
            if (criteria.getReleaseDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("releaseDate"), criteria.getReleaseDateTo()));
            }
            if (criteria.getAccessType() != null) {
                predicates.add(cb.equal(root.get("accessType"), criteria.getAccessType()));
            }
            if (criteria.getGenres() != null && !criteria.getGenres().isEmpty()) {
                predicates.add(hasAnyGenre(root, query, cb, criteria.getGenres()));
            }
            if (criteria.getPlatform() != null) {
                predicates.add(hasSupportedPlatform(root, query, cb, criteria.getPlatform().name()));
            }
            if (hasPriceFilters(criteria) || sortsByBaseGamePrice(criteria.getSortBy())) {
                predicates.add(hasMatchingBaseGameOffer(root, query, cb, criteria));
            }

            applyGameSorting(root, query, cb, criteria.getSortBy());
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> offersForGame(Long gameId, GameOfferCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = activeOfferPredicates(root, cb, gameId);

            if (criteria != null) {
                if (criteria.getPlatform() != null) {
                    predicates.add(cb.equal(root.get("platform"), criteria.getPlatform()));
                }
                if (criteria.getProductType() != null) {
                    predicates.add(cb.equal(root.get("productType"), criteria.getProductType()));
                }
                if (criteria.getDeliveryType() != null) {
                    predicates.add(cb.equal(root.get("deliveryType"), criteria.getDeliveryType()));
                }
                if (hasText(criteria.getInGameCurrencyName())) {
                    predicates.add(cb.equal(
                            cb.lower(root.get("inGameCurrencyName")),
                            criteria.getInGameCurrencyName().toLowerCase()));
                }
                if (criteria.getMinInGameAmount() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("inGameAmount"), criteria.getMinInGameAmount()));
                }
                if (criteria.getMaxInGameAmount() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("inGameAmount"), criteria.getMaxInGameAmount()));
                }
                if (hasText(criteria.getStoreName())) {
                    predicates.add(cb.like(cb.lower(root.get("storeName")), like(criteria.getStoreName())));
                }
                if (hasText(criteria.getRegion())) {
                    predicates.add(cb.equal(cb.lower(root.get("region")), criteria.getRegion().toLowerCase()));
                }
                if (hasText(criteria.getCurrency())) {
                    predicates.add(cb.equal(cb.lower(root.get("currency")), criteria.getCurrency().toLowerCase()));
                }
                if (criteria.getOfficialStore() != null) {
                    predicates.add(cb.equal(root.get("officialStore"), criteria.getOfficialStore()));
                }

                Expression<BigDecimal> finalPrice = finalPriceExpression(
                        cb,
                        root.get("discountPrice"),
                        root.get("price"));
                if (criteria.getMinPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(finalPrice, criteria.getMinPrice()));
                }
                if (criteria.getMaxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(finalPrice, criteria.getMaxPrice()));
                }
                if (Boolean.TRUE.equals(criteria.getInStock())) {
                    predicates.add(cb.greaterThan(root.get("stockQuantity"), 0));
                }

                applyOfferSorting(root.get("discountPrice"), root.get("price"), root.get("id"), query, cb, criteria.getSort());
            } else {
                applyOfferSorting(root.get("discountPrice"), root.get("price"), root.get("id"), query, cb, null);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addTextPredicates(
            Root<Game> root,
            CriteriaBuilder cb,
            GameSearchCriteria criteria,
            List<Predicate> predicates) {

        if (hasText(criteria.getQ())) {
            String pattern = like(criteria.getQ());
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("developer")), pattern),
                    cb.like(cb.lower(root.get("publisher")), pattern)
            ));
        }
        if (hasText(criteria.getTitle())) {
            predicates.add(cb.like(cb.lower(root.get("title")), like(criteria.getTitle())));
        }
        if (hasText(criteria.getDeveloper())) {
            predicates.add(cb.like(cb.lower(root.get("developer")), like(criteria.getDeveloper())));
        }
        if (hasText(criteria.getPublisher())) {
            predicates.add(cb.like(cb.lower(root.get("publisher")), like(criteria.getPublisher())));
        }
    }

    private static Predicate hasAnyGenre(
            Root<Game> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            Set<GameGenre> genres) {

        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Game> nestedGame = subquery.from(Game.class);
        SetJoin<Game, GameGenre> genreJoin = nestedGame.joinSet("genres");

        subquery.select(nestedGame.get("id"))
                .where(
                        cb.equal(nestedGame.get("id"), root.get("id")),
                        genreJoin.in(genres)
                );

        return cb.exists(subquery);
    }

    private static Predicate hasSupportedPlatform(
            Root<Game> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String platformName) {

        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Game> nestedGame = subquery.from(Game.class);
        SetJoin<Game, Platform> platformJoin = nestedGame.joinSet("supportedPlatforms");

        subquery.select(nestedGame.get("id"))
                .where(
                        cb.equal(nestedGame.get("id"), root.get("id")),
                        cb.equal(platformJoin, Platform.valueOf(platformName))
                );

        return cb.exists(subquery);
    }

    private static Predicate hasMatchingBaseGameOffer(
            Root<Game> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            GameSearchCriteria criteria) {

        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Product> offer = subquery.from(Product.class);
        List<Predicate> predicates = activeOfferPredicates(offer, cb, null);
        predicates.add(cb.equal(offer.get("game").get("id"), root.get("id")));
        predicates.add(cb.equal(offer.get("productType"), ProductType.GAME));

        Expression<BigDecimal> finalPrice = finalPriceExpression(
                cb,
                offer.get("discountPrice"),
                offer.get("price"));
        if (criteria.getMinOfferPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(finalPrice, criteria.getMinOfferPrice()));
        }
        if (criteria.getMaxOfferPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(finalPrice, criteria.getMaxOfferPrice()));
        }

        subquery.select(offer.get("id")).where(predicates.toArray(new Predicate[0]));
        return cb.exists(subquery);
    }

    private static Expression<BigDecimal> lowestBaseGamePrice(
            Root<Game> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb) {

        Subquery<BigDecimal> subquery = query.subquery(BigDecimal.class);
        Root<Product> offer = subquery.from(Product.class);
        List<Predicate> predicates = activeOfferPredicates(offer, cb, null);
        predicates.add(cb.equal(offer.get("game").get("id"), root.get("id")));
        predicates.add(cb.equal(offer.get("productType"), ProductType.GAME));

        Expression<BigDecimal> finalPrice = finalPriceExpression(
                cb,
                offer.get("discountPrice"),
                offer.get("price"));
        subquery.select(cb.min(finalPrice)).where(predicates.toArray(new Predicate[0]));
        return subquery;
    }

    private static List<Predicate> activeOfferPredicates(
            Root<Product> root,
            CriteriaBuilder cb,
            Long gameId) {

        List<Predicate> predicates = new ArrayList<>();
        if (gameId != null) {
            predicates.add(cb.equal(root.get("game").get("id"), gameId));
        }
        predicates.add(cb.isTrue(root.get("isActive")));
        predicates.add(cb.isFalse(root.get("isDeleted")));
        predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));
        return predicates;
    }

    private static boolean hasPriceFilters(GameSearchCriteria criteria) {
        return criteria.getMinOfferPrice() != null || criteria.getMaxOfferPrice() != null;
    }

    private static boolean sortsByBaseGamePrice(GameBrowseSort sort) {
        return sort == GameBrowseSort.PRICE_LOW_TO_HIGH
                || sort == GameBrowseSort.PRICE_HIGH_TO_LOW;
    }

    private static Expression<BigDecimal> finalPriceExpression(
            CriteriaBuilder cb,
            Expression<BigDecimal> discountPrice,
            Expression<BigDecimal> price) {
        return cb.coalesce(discountPrice, price);
    }

    private static void applyGameSorting(
            Root<Game> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            GameBrowseSort sort) {

        if (Long.class.equals(query.getResultType()) || long.class.equals(query.getResultType())) {
            return;
        }

        GameBrowseSort safeSort = sort == null ? GameBrowseSort.ALPHABETICAL : sort;
        switch (safeSort) {
            case NEWEST -> query.orderBy(cb.desc(root.get("releaseDate")), cb.asc(root.get("title")));
            case OLDEST -> query.orderBy(cb.asc(root.get("releaseDate")), cb.asc(root.get("title")));
            case PRICE_LOW_TO_HIGH -> query.orderBy(
                    cb.asc(lowestBaseGamePrice(root, query, cb)),
                    cb.asc(root.get("title")));
            case PRICE_HIGH_TO_LOW -> query.orderBy(
                    cb.desc(lowestBaseGamePrice(root, query, cb)),
                    cb.asc(root.get("title")));
            case ALPHABETICAL -> query.orderBy(cb.asc(root.get("title")));
        }
    }

    private static void applyOfferSorting(
            Expression<BigDecimal> discountPrice,
            Expression<BigDecimal> price,
            Expression<Long> id,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            com.example.gamesphere.enums.GameOfferSort sort) {

        if (Long.class.equals(query.getResultType()) || long.class.equals(query.getResultType())) {
            return;
        }

        Expression<BigDecimal> finalPrice = cb.coalesce(discountPrice, price);
        com.example.gamesphere.enums.GameOfferSort safeSort = sort == null
                ? com.example.gamesphere.enums.GameOfferSort.PRICE_ASC
                : sort;

        switch (safeSort) {
            case PRICE_DESC -> query.orderBy(cb.desc(finalPrice));
            case NEWEST -> query.orderBy(cb.desc(id));
            case PRICE_ASC -> query.orderBy(cb.asc(finalPrice));
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String like(String value) {
        return "%" + value.toLowerCase() + "%";
    }
}
