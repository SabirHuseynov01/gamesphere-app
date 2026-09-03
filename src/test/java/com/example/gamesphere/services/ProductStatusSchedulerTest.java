package com.example.gamesphere.scheduler;

import com.example.gamesphere.entity.Product;
import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.enums.ProductStatus;
import com.example.gamesphere.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductStatusSchedulerTest {

    @Mock
    private ProductRepository productRepository;

    @Test
    void onlyStockManagedOffersAreMarkedOutOfStock() {
        Product stockManagedProduct = new Product();
        stockManagedProduct.setDeliveryType(DeliveryType.DIGITAL_CODE);
        stockManagedProduct.setStatus(ProductStatus.ACTIVE);

        List<DeliveryType> excludedDeliveryTypes = List.of(
                DeliveryType.STORE_REDIRECT,
                DeliveryType.EXTERNAL_MARKET,
                DeliveryType.PLAYER_ID_TOP_UP);
        when(productRepository
                .findByIsActiveTrueAndStockQuantityLessThanEqualAndStatusAndDeliveryTypeNotIn(
                        0, ProductStatus.ACTIVE, excludedDeliveryTypes))
                .thenReturn(List.of(stockManagedProduct));

        new ProductStatusScheduler(productRepository).markOutOfStockProducts();

        assertThat(stockManagedProduct.getStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK);
        verify(productRepository).findByIsActiveTrueAndStockQuantityLessThanEqualAndStatusAndDeliveryTypeNotIn(
                0, ProductStatus.ACTIVE, excludedDeliveryTypes);
    }
}
