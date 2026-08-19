package com.example.gamesphere.scheduler;

import com.example.gamesphere.entity.Product;
import com.example.gamesphere.enums.ProductStatus;
import com.example.gamesphere.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductStatusScheduler {

    private final ProductRepository productRepository;

    @Scheduled(fixedRate = 600_000)
    @Transactional
    public void markOutOfStockProducts() {
        List<Product> products = productRepository.findByIsActiveTrueAndStockQuantityLessThanEqualAndStatus(
                0,
                ProductStatus.ACTIVE);

        products.forEach(product -> product.setStatus(ProductStatus.OUT_OF_STOCK));

        if (!products.isEmpty()) {
            log.info("Product status scheduler marked {} products as OUT_OF_STOCK", products.size());
        }
    }
}
