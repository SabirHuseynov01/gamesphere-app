package com.example.gamesphere.service;

import com.example.gamesphere.dto.request.CreateOrderRequest;
import com.example.gamesphere.dto.request.OrderStatusUpdateRequest;
import com.example.gamesphere.dto.response.OrderResponse;
import com.example.gamesphere.dto.response.PageResponse;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.event.OrderCreatedEvent;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.mapper.OrderMapper;
import com.example.gamesphere.repository.OrderRepository;
import com.example.gamesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final OrderProcessingService orderProcessingService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        if (!request.hasProducts()) {
            throw new BusinessException("The order must contain at least one product.");
        }

        User user = getCurrentUser();
        Order order = orderProcessingService.processOrder(user, request);
        eventPublisher.publishEvent(new OrderCreatedEvent(order));
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse createOrderFromCart() {
        User user = getCurrentUser();
        Order order = orderProcessingService.processCartOrder(user);
        eventPublisher.publishEvent(new OrderCreatedEvent(order));
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        verifyOwnership(order, user);
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        User user = getCurrentUser();
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
        verifyOwnership(order, user);
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orderMapper.toResponseList(orders);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyOrders(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Page<Order> orders = orderRepository.findByUserEmail(email, pageable);
        return PageResponse.of(orders.map(orderMapper::toResponse));
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(request.getStatus());
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toResponse(updatedOrder);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void verifyOwnership(Order order, User user) {
        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order not found");
        }
    }
}

