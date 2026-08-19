package com.example.gamesphere.listener;

import com.example.gamesphere.enums.NotificationType;
import com.example.gamesphere.event.OrderCreatedEvent;
import com.example.gamesphere.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final NotificationService notificationService;


    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        notificationService.createNotification(
                event.order().getUser(),
                NotificationType.ORDER_CREATED,
                "Order Created",
                "Your order " + event.order().getOrderNumber() + " was created successfully.");
    }
}
