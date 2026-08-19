package com.example.gamesphere.scheduler;

import com.example.gamesphere.entity.Product;
import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfferRefreshScheduler {

    private final ProductRepository productRepository;

    @Scheduled(fixedRate = 1_800_000)
    @Transactional
    public void refreshExternalOfferTimestamps() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(6);
        List<Product> products = productRepository.findByIsActiveTrueAndDeliveryTypeInAndLastCheckedAtBefore(
                List.of(
                        DeliveryType.STORE_REDIRECT,
                        DeliveryType.DIGITAL_CODE,
                        DeliveryType.PLAYER_ID_TOP_UP,
                        DeliveryType.EXTERNAL_MARKET),
                cutoff);

        LocalDateTime refreshedAt = LocalDateTime.now();
        products.forEach(product -> product.setLastCheckedAt(refreshedAt));

        if (!products.isEmpty()) {
            log.info("Offer refresh scheduler refreshed {} external offers", products.size());
        }
    }
}
