package com.example.gamesphere.mapper;

import com.example.gamesphere.dto.response.NotificationResponse;
import com.example.gamesphere.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
