package com.example.gamesphere.mapper;

import com.example.gamesphere.dto.request.RegisterRequest;
import com.example.gamesphere.dto.request.UserProfileUpdateRequest;
import com.example.gamesphere.dto.response.UserProfileResponse;
import com.example.gamesphere.entity.Role;
import com.example.gamesphere.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "seller", constant = "false")
    @Mapping(target = "balance", constant = "0.0")
    User toEntity(RegisterRequest request);

    UserProfileResponse toProfileResponse(User user);

    /** Lets MapStruct turn the user's Set<Role> into the plain names the UI reads. */
    default String roleName(Role role) {
        return role == null ? null : role.getName();
    }

    void updateProfile(@MappingTarget User user, UserProfileUpdateRequest request);
}
