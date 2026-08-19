package com.example.gamesphere.services;

import com.example.gamesphere.entity.DigitalCode;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.OrderItem;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.entity.UserEntitlement;
import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.enums.EntitlementStatus;
import com.example.gamesphere.enums.OrderStatus;
import com.example.gamesphere.repository.UserEntitlementRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.service.DigitalCodeService;
import com.example.gamesphere.service.EntitlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntitlementServiceTest extends ServiceTestSupport {

    @Mock UserEntitlementRepository entitlementRepository;
    @Mock UserRepository userRepository;
    @Mock DigitalCodeService digitalCodeService;
    private EntitlementService entitlementService;

    @BeforeEach
    void setUp() {
        entitlementService = new EntitlementService(
                entitlementRepository,
                userRepository,
                digitalCodeService);
    }

    @Test
    void paidDigitalCodeOrderAllocatesCodeAndCreatesLibraryOwnership() {
        User user = user(1L, "user@mail.com");
        Product product = withId(new Product(), 4L);
        product.setName("Mortal Kombat 1 Steam Key");
        product.setDeliveryType(DeliveryType.DIGITAL_CODE);
        OrderItem item = withId(new OrderItem(), 8L);
        item.setProduct(product);
        item.setGift(false);
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PAID);
        order.setOrderItems(Set.of(item));
        DigitalCode code = withId(DigitalCode.builder().codeValue("MK1-TEST-CODE").build(), 3L);
        when(entitlementRepository.existsByUserIdAndProductId(1L, 4L)).thenReturn(false);
        when(digitalCodeService.allocate(product)).thenReturn(code);

        entitlementService.grantForPaidOrder(order);

        ArgumentCaptor<UserEntitlement> captor = ArgumentCaptor.forClass(UserEntitlement.class);
        verify(entitlementRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getProduct()).isSameAs(product);
        assertThat(captor.getValue().getDigitalCode()).isSameAs(code);
    }

    @Test
    void currentUserCanReadOwnedLibraryItem() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        Product product = withId(new Product(), 4L);
        product.setName("Mortal Kombat 1");
        UserEntitlement entitlement = withId(UserEntitlement.builder()
                .user(user)
                .product(product)
                .status(EntitlementStatus.ACTIVE)
                .grantedAt(LocalDateTime.now())
                .build(), 12L);
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(entitlementRepository.findByIdAndUserId(12L, 1L))
                .thenReturn(Optional.of(entitlement));

        assertThat(entitlementService.getMyLibraryItem(12L).getProductName())
                .isEqualTo("Mortal Kombat 1");
    }
}
