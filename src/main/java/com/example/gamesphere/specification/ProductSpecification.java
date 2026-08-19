package com.example.gamesphere.specification;

import com.example.gamesphere.dto.request.ProductFilterRequest;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.enums.ProductStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> withFilter(ProductFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                predicates.add(cb.equal(root.get("isActive"), true));
                predicates.add(cb.equal(root.get("isDeleted"), false));
                predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (filter.getSearch() != null && !filter.getSearch().isEmpty()) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), searchPattern),
                        cb.like(cb.lower(root.get("description")), searchPattern)
                ));
            }

            if (filter.getGenre() != null && !filter.getGenre().isEmpty()) {
                predicates.add(cb.equal(root.get("genre"), filter.getGenre()));
            }

            if (filter.getPlatform() != null) {
                predicates.add(cb.equal(root.get("platform"), filter.getPlatform()));
            }

            if (filter.getProductType() != null) {
                predicates.add(cb.equal(root.get("productType"), filter.getProductType()));
            }

            if (filter.getEditionName() != null && !filter.getEditionName().isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("editionName")),
                        filter.getEditionName().toLowerCase()));
            }

            if (filter.getCatalogSection() != null) {
                predicates.add(cb.equal(root.get("catalogSection"), filter.getCatalogSection()));
            }

            if (filter.getDeliveryType() != null) {
                predicates.add(cb.equal(root.get("deliveryType"), filter.getDeliveryType()));
            }

            if (filter.getInGameCurrencyName() != null && !filter.getInGameCurrencyName().isEmpty()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("inGameCurrencyName")),
                        filter.getInGameCurrencyName().toLowerCase()));
            }

            if (filter.getMinInGameAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("inGameAmount"), filter.getMinInGameAmount()));
            }

            if (filter.getMaxInGameAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("inGameAmount"), filter.getMaxInGameAmount()));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            if (filter.getInStock() != null && filter.getInStock()) {
                predicates.add(cb.greaterThan(root.get("stockQuantity"), 0));
            }

            predicates.add(cb.equal(root.get("isActive"), true));
            predicates.add(cb.equal(root.get("isDeleted"), false));
            predicates.add(cb.notEqual(root.get("status"), ProductStatus.DELETED));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
