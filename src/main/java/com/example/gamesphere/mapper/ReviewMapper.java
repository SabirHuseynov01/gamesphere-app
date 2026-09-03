package com.example.gamesphere.mapper;

import com.example.gamesphere.dto.request.ReviewCreateRequest;
import com.example.gamesphere.dto.response.ReviewResponse;
import com.example.gamesphere.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    @Mapping(target = "approved", constant = "false")
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "game", ignore = true)
    @Mapping(target = "user", ignore = true)
    Review toEntity(ReviewCreateRequest request);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "gameId", source = "game.id")
    ReviewResponse toResponse(Review review);
}
