package com.example.gamesphere.dto.request;

import com.example.gamesphere.enums.TopUpFulfillmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTopUpFulfillmentRequest {

    @NotNull
    private TopUpFulfillmentStatus status;

    @Size(max = 255)
    private String providerReference;

    @Size(max = 1000)
    private String failureReason;
}
