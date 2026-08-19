package com.example.gamesphere.mapper;

import com.example.gamesphere.dto.response.TopUpFulfillmentResponse;
import com.example.gamesphere.entity.TopUpFulfillment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TopUpFulfillmentMapper {

    @Mapping(target = "orderId", source = "orderItem.order.id")
    @Mapping(target = "orderNumber", source = "orderItem.order.orderNumber")
    @Mapping(target = "orderItemId", source = "orderItem.id")
    @Mapping(target = "productId", source = "orderItem.product.id")
    @Mapping(target = "productName", source = "orderItem.product.name")
    @Mapping(target = "playerAccountId", source = "orderItem.playerAccountId")
    @Mapping(target = "inGameCurrencyName", source = "orderItem.product.inGameCurrencyName")
    @Mapping(target = "inGameAmount", source = "orderItem.product.inGameAmount")
    @Mapping(target = "bonusAmount", source = "orderItem.product.bonusAmount")
    TopUpFulfillmentResponse toResponse(TopUpFulfillment fulfillment);
}
