package com.example.gamesphere.service;

import com.example.gamesphere.dto.response.LibraryItemResponse;
import com.example.gamesphere.entity.DigitalCode;
import com.example.gamesphere.entity.Gift;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.OrderItem;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.entity.UserEntitlement;
import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.enums.EntitlementStatus;
import com.example.gamesphere.enums.OrderStatus;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.repository.UserEntitlementRepository;
import com.example.gamesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EntitlementService {

    private final UserEntitlementRepository entitlementRepository;
    private final UserRepository userRepository;
    private final DigitalCodeService digitalCodeService;

    @Transactional
    public void grantForPaidOrder(Order order) {
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("Entitlements can only be granted for a paid order.");
        }
        order.getOrderItems().stream()
                .filter(item -> !item.isGift())
                .filter(item -> supportsLibrary(item.getProduct()))
                .forEach(item -> grant(order.getUser(), item));
    }

    @Transactional
    public void grantGift(User recipient, Gift gift) {
        if (!supportsLibrary(gift.getProduct())) {
            return;
        }
        grant(recipient, gift.getOrderItem());
    }

    @Transactional(readOnly = true)
    public List<LibraryItemResponse> getMyLibrary() {
        User user = getCurrentUser();
        return entitlementRepository.findAllByUserIdOrderByGrantedAtDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LibraryItemResponse getMyLibraryItem(Long id) {
        User user = getCurrentUser();
        return entitlementRepository.findByIdAndUserId(id, user.getId())
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Library item not found"));
    }

    private void grant(User user, OrderItem orderItem) {
        Product product = orderItem.getProduct();
        if (entitlementRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new BusinessException("User already owns " + product.getName() + ".");
        }

        DigitalCode code = product.getDeliveryType() == DeliveryType.DIGITAL_CODE
                ? digitalCodeService.allocate(product)
                : null;

        entitlementRepository.save(UserEntitlement.builder()
                .user(user)
                .product(product)
                .orderItem(orderItem)
                .digitalCode(code)
                .status(EntitlementStatus.ACTIVE)
                .grantedAt(LocalDateTime.now())
                .build());
    }

    private boolean supportsLibrary(Product product) {
        return product.getDeliveryType() == DeliveryType.DIGITAL_CODE
                || product.getDeliveryType() == DeliveryType.INTERNAL_LIBRARY;
    }

    private LibraryItemResponse toResponse(UserEntitlement entitlement) {
        Product product = entitlement.getProduct();
        return LibraryItemResponse.builder()
                .id(entitlement.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productSlug(product.getSlug())
                .imageUrl(product.getImageUrl())
                .platform(product.getPlatform())
                .productType(product.getProductType())
                .deliveryType(product.getDeliveryType())
                .status(entitlement.getStatus())
                .grantedAt(entitlement.getGrantedAt())
                .digitalCode(entitlement.getDigitalCode() == null
                        ? null
                        : entitlement.getDigitalCode().getCodeValue())
                .redemptionUrl(product.getRedemptionUrl())
                .redemptionInstructions(product.getRedemptionInstructions())
                .build();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
