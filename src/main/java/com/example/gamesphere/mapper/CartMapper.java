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
    CartResponse toResponse(Cart cart);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "price", source = "priceAtAddTime")
    CartItemResponse toCartItemResponse(CartItem cartItem);

    default BigDecimal calculateTotal(Cart cart) {
        if (cart.getCartItems() == null) return BigDecimal.ZERO;
        return cart.getCartItems().stream()
                .map(item -> item.getPriceAtAddTime().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
