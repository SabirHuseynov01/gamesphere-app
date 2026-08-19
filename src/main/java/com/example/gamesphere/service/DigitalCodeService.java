package com.example.gamesphere.service;

import com.example.gamesphere.dto.request.DigitalCodeBatchRequest;
import com.example.gamesphere.dto.response.DigitalCodeStockResponse;
import com.example.gamesphere.entity.DigitalCode;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.enums.DigitalCodeStatus;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.repository.DigitalCodeRepository;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;

@Service
@RequiredArgsConstructor
public class DigitalCodeService {

    private final DigitalCodeRepository digitalCodeRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public DigitalCodeStockResponse addCodes(Long productId, DigitalCodeBatchRequest request) {
        User seller = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
            throw new ResourceNotFoundException("Product not found");
        }
        if (product.getDeliveryType() != DeliveryType.DIGITAL_CODE) {
            throw new BusinessException("Digital codes can only be added to DIGITAL_CODE products.");
        }

        int added = 0;
        for (String value : new LinkedHashSet<>(request.getCodes())) {
            String code = value.trim();
            if (digitalCodeRepository.existsByCodeValue(code)) {
                throw new BusinessException("Digital code already exists: " + code);
            }
            digitalCodeRepository.save(DigitalCode.builder()
                    .product(product)
                    .codeValue(code)
                    .build());
            added++;
        }

        long available = digitalCodeRepository.countByProductIdAndStatus(
                productId, DigitalCodeStatus.AVAILABLE);
        return new DigitalCodeStockResponse(productId, added, available);
    }

    @Transactional
    public DigitalCode allocate(Product product) {
        DigitalCode code = digitalCodeRepository.findAvailableForUpdate(
                        product.getId(), DigitalCodeStatus.AVAILABLE, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "No digital code is available for " + product.getName() + "."));
        code.setStatus(DigitalCodeStatus.DELIVERED);
        code.setDeliveredAt(LocalDateTime.now());
        return digitalCodeRepository.save(code);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
