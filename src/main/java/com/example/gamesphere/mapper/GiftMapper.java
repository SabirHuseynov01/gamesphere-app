package com.example.gamesphere.mapper;

import com.example.gamesphere.dto.response.GiftResponse;
import com.example.gamesphere.entity.Gift;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GiftMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "senderUsername", source = "sender.username")
    GiftResponse toResponse(Gift gift);
}
