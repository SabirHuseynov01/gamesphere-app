package com.example.gamesphere.mapper;

import com.example.gamesphere.dto.request.SellerProfileRequest;
import com.example.gamesphere.dto.response.SellerProfileResponse;
import com.example.gamesphere.entity.SellerProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SellerProfileMapper {

    @Mapping(target = "approved", constant = "false")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    SellerProfile toEntity(SellerProfileRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    SellerProfileResponse toResponse(SellerProfile sellerProfile);
}
