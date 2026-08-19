package com.example.gamesphere.scheduler;

import com.example.gamesphere.entity.Gift;
import com.example.gamesphere.enums.GiftStatus;
import com.example.gamesphere.repository.GiftRepository;
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
public class GiftExpiryScheduler {

    private final GiftRepository giftRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireUnclaimedGifts() {
        List<Gift> expiredGifts = giftRepository.findByStatusAndExpiresAtBefore(
                GiftStatus.PENDING,
                LocalDateTime.now());

        expiredGifts.forEach(gift -> gift.setStatus(GiftStatus.EXPIRED));

        if (!expiredGifts.isEmpty()) {
            log.info("Gift expiry scheduler marked {} gifts as expired", expiredGifts.size());
        }
    }
}
