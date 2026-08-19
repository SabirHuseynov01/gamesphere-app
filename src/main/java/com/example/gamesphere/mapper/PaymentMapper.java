package com.example.gamesphere.mapper;

import com.example.gamesphere.dto.request.BillingInfoRequest;
import com.example.gamesphere.dto.response.PaymentResponse;
import com.example.gamesphere.entity.BillingInfo;
import com.example.gamesphere.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    BillingInfo toBillingInfo(BillingInfoRequest request);

    @Mapping(target = "orderId", source = "order.id")
    PaymentResponse toResponse(Payment payment);
}
