package com.example.gamesphere.service;

import com.example.gamesphere.dto.request.CreatePaymentRequest;
import com.example.gamesphere.dto.response.PaymentResponse;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.Payment;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.OrderStatus;
import com.example.gamesphere.enums.PaymentMethod;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.exception.InsufficientBalanceException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.mapper.PaymentMapper;
import com.example.gamesphere.repository.OrderRepository;
import com.example.gamesphere.repository.PaymentRepository;
import com.example.gamesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentCompletionService paymentCompletionService;

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Order order = orderRepository.findByIdForUpdate(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        User user = userRepository.findByIdForUpdate(order.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getEmail().equals(email)) {
            throw new BusinessException("You can only pay for your own orders.");
        }

        if (request.getPaymentMethod() != PaymentMethod.BALANCE) {
            throw new BusinessException("Use the dedicated checkout endpoint for external payment methods.");
        }

        if (order.getStatus() == OrderStatus.PAID) {
            throw new BusinessException("Order is already paid.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REFUNDED) {
            throw new BusinessException("This order cannot be paid because its status is " + order.getStatus() + ".");
        }

        paymentRepository.findByOrderId(order.getId())
                .ifPresent(existing -> {
                    throw new BusinessException("Payment already exists for this order.");
                });

        if (user.getBalance() < order.getTotalAmount().doubleValue()) {
            throw new InsufficientBalanceException("Your balance is not sufficient");
        }

        user.setBalance(user.getBalance() - order.getTotalAmount().doubleValue());
        userRepository.save(user);

        Payment savedPayment = paymentCompletionService.complete(
                order,
                user,
                PaymentMethod.BALANCE,
                paymentMapper.toBillingInfo(request.getBillingInfo()),
                java.util.UUID.randomUUID().toString());

        return paymentMapper.toResponse(savedPayment);
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        User user = getCurrentUser();
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (payment.getUser() == null || !payment.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Payment not found");
        }
        return paymentMapper.toResponse(payment);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}