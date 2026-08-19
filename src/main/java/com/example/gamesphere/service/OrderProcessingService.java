package com.example.gamesphere.service;

import com.example.gamesphere.dto.request.CreateOrderItemRequest;
import com.example.gamesphere.dto.request.CreateOrderRequest;
import com.example.gamesphere.entity.Cart;
import com.example.gamesphere.entity.CartItem;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.OrderItem;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.enums.OrderStatus;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.exception.ProductOutOfStockException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.repository.CartRepository;
import com.example.gamesphere.repository.OrderRepository;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.util.DiscountCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final DiscountCalculator discountCalculator;

    @Transactional
    public Order processOrder(User user, CreateOrderRequest request) {
        Order order = createPendingOrder(user);
        Set<OrderItem> orderItems = new HashSet<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderItemRequest requestedItem : resolveRequestedItems(request)) {
            Product product = productRepository.findByIdForUpdate(requestedItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found: " + requestedItem.getProductId()));

            int quantity = requestedItem.resolvedQuantity();
            validateProduct(product, quantity);

            String playerAccountId = resolvePlayerAccountId(
                    product,
                    requestedItem.getPlayerAccountId());

            BigDecimal priceAtPurchase = discountCalculator.finalPrice(
                    product.getPrice(),
                    product.getDiscountPrice());

            OrderItem item = createOrderItem(
                    order,
                    product,
                    quantity,
                    priceAtPurchase,
                    requestedItem.isGift(),
                    requestedItem.getRecipientEmail(),
                    requestedItem.getGiftMessage(),
                    playerAccountId);

            orderItems.add(item);
            totalAmount = totalAmount.add(
                    priceAtPurchase.multiply(BigDecimal.valueOf(quantity)));
            product.setStockQuantity(product.getStockQuantity() - quantity);
        }

        return saveOrder(order, orderItems, totalAmount);
    }

    @Transactional
    public Order processCartOrder(User user) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Cart is empty."));

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new BusinessException("Cart is empty.");
        }

        Order order = createPendingOrder(user);
        Set<OrderItem> orderItems = new HashSet<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = productRepository.findByIdForUpdate(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found: " + cartItem.getProduct().getId()));
            int quantity = cartItem.getQuantity();
            validateProduct(product, quantity);

            String playerAccountId = resolvePlayerAccountId(
                    product,
                    cartItem.getPlayerAccountId());

            BigDecimal priceAtPurchase = discountCalculator.finalPrice(
                    product.getPrice(),
                    product.getDiscountPrice());

            OrderItem orderItem = createOrderItem(
                    order,
                    product,
                    quantity,
                    priceAtPurchase,
                    false,
                    null,
                    null,
                    playerAccountId);

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(
                    priceAtPurchase.multiply(BigDecimal.valueOf(quantity)));
            product.setStockQuantity(product.getStockQuantity() - quantity);
        }

        Order savedOrder = saveOrder(order, orderItems, totalAmount);
        cart.getCartItems().clear();
        cartRepository.save(cart);
        return savedOrder;
    }

    private Order createPendingOrder(User user) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(
                "GS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setStatus(OrderStatus.PENDING);
        return order;
    }

    private OrderItem createOrderItem(
            Order order,
            Product product,
            int quantity,
            BigDecimal priceAtPurchase,
            boolean gift,
            String recipientEmail,
            String giftMessage,
            String playerAccountId) {

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setPriceAtPurchase(priceAtPurchase);
        item.setGift(gift);
        item.setGiftRecipientEmail(recipientEmail);
        item.setGiftMessage(giftMessage);
        item.setPlayerAccountId(playerAccountId);
        return item;
    }

    private Order saveOrder(
            Order order,
            Set<OrderItem> orderItems,
            BigDecimal totalAmount) {

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        return orderRepository.save(order);
    }

    private void validateProduct(Product product, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be greater than zero.");
        }

        if (!product.isActive() || product.isDeleted()) {
            throw new BusinessException(product.getName() + " is not available.");
        }

        if (product.getDeliveryType() == DeliveryType.STORE_REDIRECT
                || product.getDeliveryType() == DeliveryType.EXTERNAL_MARKET) {
            throw new BusinessException(
                    product.getName() + " must be purchased on the external store.");
        }

        if (product.getStockQuantity() < quantity) {
            throw new ProductOutOfStockException(product.getName() + " out of stock");
        }
    }

    private String resolvePlayerAccountId(Product product, String playerAccountId) {
        String normalizedId = playerAccountId == null ? null : playerAccountId.trim();

        if (normalizedId != null && normalizedId.isBlank()) {
            normalizedId = null;
        }

        if (product.isRequiresPlayerId() && normalizedId == null) {
            String label = product.getPlayerIdLabel() == null
                    || product.getPlayerIdLabel().isBlank()
                    ? "Player account ID"
                    : product.getPlayerIdLabel();

            throw new BusinessException(label + " is required for " + product.getName() + ".");
        }

        return product.isRequiresPlayerId() ? normalizedId : null;
    }

    private List<CreateOrderItemRequest> resolveRequestedItems(CreateOrderRequest request) {
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            return request.getItems();
        }

        List<CreateOrderItemRequest> items = new ArrayList<>();
        if (request.getProductIds() != null) {
            for (Long productId : request.getProductIds()) {
                CreateOrderItemRequest item = new CreateOrderItemRequest();
                item.setProductId(productId);
                item.setQuantity(1);
                items.add(item);
            }
        }
        return items;
    }
}

