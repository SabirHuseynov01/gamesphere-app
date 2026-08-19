package com.example.gamesphere.mapper;

import com.example.gamesphere.dto.response.WishlistResponse;
import com.example.gamesphere.entity.Wishlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface WishlistMapper {

    @Mapping(target = "wishlistId", source = "id")
    WishlistResponse toResponse(Wishlist wishlist);
}
