package com.example.gamesphere.mapper;

import com.example.gamesphere.dto.response.OrderItemResponse;
import com.example.gamesphere.dto.response.OrderResponse;
import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "items", source = "orderItems")
    OrderResponse toResponse(Order order);

    List<OrderResponse> toResponseList(List<Order> orders);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "price", source = "priceAtPurchase")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
}
