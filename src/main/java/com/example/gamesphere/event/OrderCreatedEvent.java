package com.example.gamesphere.event;

import com.example.gamesphere.entity.Order;

public record OrderCreatedEvent(Order order) {
}
