package com.example.gamesphere.repository;


import com.example.gamesphere.entity.Product;
import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    List<Product> findBySellerIdAndIsActiveTrueAndIsDeletedFalse(Long sellerId);
    List<Product> findByIsActiveTrue();
    Optional<Product> findBySlug(String slug);
    Optional<Product> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
    Optional<Product> findBySlugAndIsActiveTrueAndIsDeletedFalse(String slug);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select product from Product product where product.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
    boolean existsBySlug(String slug);
    List<Product> findTop10ByGenreAndIdNotAndIsActiveTrue(String genre, Long id);
    List<Product> findByIsActiveTrueAndStockQuantityLessThanEqualAndStatus(
            int stockQuantity, ProductStatus status);
    List<Product> findByIsActiveTrueAndDeliveryTypeInAndLastCheckedAtBefore(
            List<DeliveryType> deliveryTypes, LocalDateTime cutoff);

    @Query("""
            select p from Product p
            left join Review r on r.product = p
            where p.isActive = true
            group by p
            order by count(r.id) desc
            """)
    List<Product> findTopReviewedProducts();

    @Query("""
            select distinct p from Product p
            join p.categories c
            where c.id in :categoryIds
              and p.id not in :excludedProductIds
              and p.isActive = true
            """)
    List<Product> findRecommendedByCategoryIds(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("excludedProductIds") List<Long> excludedProductIds);
}