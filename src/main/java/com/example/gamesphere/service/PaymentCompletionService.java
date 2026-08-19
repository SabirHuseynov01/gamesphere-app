package com.example.gamesphere.service;

import com.example.gamesphere.entity.BillingInfo;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.Payment;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.NotificationType;
import com.example.gamesphere.enums.OrderStatus;
import com.example.gamesphere.enums.PaymentMethod;
import com.example.gamesphere.enums.PaymentStatus;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.repository.OrderRepository;
import com.example.gamesphere.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCompletionService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final GiftService giftService;
    private final EntitlementService entitlementService;
    private final TopUpFulfillmentService topUpFulfillmentService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Transactional
    public Payment complete(Order order,
                            User user,
                            PaymentMethod method,
                            BillingInfo billingInfo,
                            String transactionId) {
        Payment existing = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (existing != null) {
            if (existing.getStatus() == PaymentStatus.PAID) {
                return existing;
            }
            throw new BusinessException("A payment already exists for this order.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Only PENDING orders can be paid.");
        }

        Payment payment = Payment.builder()
                .user(user)
                .order(order)
                .amount(order.getTotalAmount())
                .method(method)
                .billingInfo(billingInfo)
                .status(PaymentStatus.PAID)
                .transactionId(transactionId)
                .build();

        order.setStatus(OrderStatus.PAID);
        order.setPaymentMethod(method.name());
        order.setTransactionId(transactionId);
        orderRepository.save(order);
        Payment savedPayment = paymentRepository.save(payment);

        giftService.createGiftsForPaidOrder(order);
        entitlementService.grantForPaidOrder(order);
        topUpFulfillmentService.createPendingForPaidOrder(order);
        notificationService.createNotification(
                user,
                NotificationType.PAYMENT_SUCCESS,
                "Payment successful",
                "Your payment for order " + order.getOrderNumber() + " was completed.");
        emailService.sendPaymentSuccessEmail(user, order, savedPayment);
        return savedPayment;
    }
}
