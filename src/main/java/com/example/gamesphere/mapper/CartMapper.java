package com.example.gamesphere.mapper;

import com.example.gamesphere.dto.response.CartItemResponse;
import com.example.gamesphere.dto.response.CartResponse;
import com.example.gamesphere.entity.Cart;
import com.example.gamesphere.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.math.BigDecimal;

@Mapper(componentModel = "spring")

public interface CartMapper {

    @Mapping(target = "items", source = "cartItems")
    @Mapping(target = "cartId", source = "id")
    @Mapping(target = "totalAmount", expression = "java(calculateTotal(cart))")
    @Mapping(target = "currency", expression = "java(resolveCurrency(cart))")
    CartResponse toResponse(Cart cart);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "price", source = "priceAtAddTime")
    @Mapping(target = "currency", source = "product.currency")
    @Mapping(target = "requiresPlayerId", source = "product.requiresPlayerId")
    @Mapping(target = "playerIdLabel", source = "product.playerIdLabel")
    CartItemResponse toCartItemResponse(CartItem cartItem);

    /** Currency of the cart total, taken from the first item a cart holds. */
    default String resolveCurrency(Cart cart) {
        if (cart.getCartItems() == null) return null;
        return cart.getCartItems().stream()
                .map(item -> item.getProduct().getCurrency())
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    default BigDecimal calculateTotal(Cart cart) {
        if (cart.getCartItems() == null) return BigDecimal.ZERO;
        return cart.getCartItems().stream()
                .map(item -> item.getPriceAtAddTime().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
