package com.example.gamesphere.services;

import com.example.gamesphere.dto.request.BillingInfoRequest;
import com.example.gamesphere.dto.request.CreatePaymentRequest;
import com.example.gamesphere.dto.response.PaymentResponse;
import com.example.gamesphere.entity.BillingInfo;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.Payment;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.OrderStatus;
import com.example.gamesphere.enums.PaymentMethod;
import com.example.gamesphere.exception.InsufficientBalanceException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.mapper.PaymentMapper;
import com.example.gamesphere.repository.OrderRepository;
import com.example.gamesphere.repository.PaymentRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.service.PaymentCompletionService;
import com.example.gamesphere.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest extends ServiceTestSupport {

    @Mock PaymentRepository paymentRepository;
    @Mock OrderRepository orderRepository;
    @Mock UserRepository userRepository;
    @Mock PaymentMapper paymentMapper;
    @Mock
    PaymentCompletionService paymentCompletionService;
    @InjectMocks
    PaymentService paymentService;

    @Test
    void balancePaymentLocksResourcesDeductsBalanceAndDelegatesCompletion() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        user.setBalance(100.0);
        Order order = order(8L, user, "59.99");
        CreatePaymentRequest request = new CreatePaymentRequest(8L, PaymentMethod.BALANCE, new BillingInfoRequest());
        PaymentResponse expected = new PaymentResponse();

        when(orderRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(order));
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(paymentRepository.findByOrderId(8L)).thenReturn(Optional.empty());
        when(paymentMapper.toBillingInfo(request.getBillingInfo())).thenReturn(new BillingInfo());
        Payment completedPayment = Payment.builder().order(order).user(user).build();
        when(paymentCompletionService.complete(
                org.mockito.ArgumentMatchers.eq(order),
                org.mockito.ArgumentMatchers.eq(user),
                org.mockito.ArgumentMatchers.eq(PaymentMethod.BALANCE),
                org.mockito.ArgumentMatchers.any(BillingInfo.class),
                org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(completedPayment);
        when(paymentMapper.toResponse(completedPayment)).thenReturn(expected);

        assertThat(paymentService.createPayment(request)).isSameAs(expected);
        assertThat(user.getBalance()).isEqualTo(40.01);
        verify(userRepository).save(user);
        verify(paymentCompletionService).complete(
                org.mockito.ArgumentMatchers.eq(order),
                org.mockito.ArgumentMatchers.eq(user),
                org.mockito.ArgumentMatchers.eq(PaymentMethod.BALANCE),
                org.mockito.ArgumentMatchers.any(BillingInfo.class),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void insufficientBalanceLeavesOrderPending() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        user.setBalance(10.0);
        Order order = order(8L, user, "59.99");
        CreatePaymentRequest request = new CreatePaymentRequest(8L, PaymentMethod.BALANCE, null);
        when(orderRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(order));
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(paymentRepository.findByOrderId(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(InsufficientBalanceException.class);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(user.getBalance()).isEqualTo(10.0);
    }

    @Test
    void getPaymentByOrderIdHidesAnotherUsersPayment() {
        authenticate("user@mail.com");
        User currentUser = user(1L, "user@mail.com");
        User owner = user(2L, "owner@mail.com");
        Payment payment = new Payment();
        payment.setUser(owner);
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(currentUser));
        when(paymentRepository.findByOrderId(8L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.getPaymentByOrderId(8L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Payment not found");
    }

    private Order order(Long id, User user, String total) {
        Order order = new Order();
        order.setId(id);
        order.setUser(user);
        order.setOrderNumber("GS-TEST");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal(total));
        return order;
    }
}


