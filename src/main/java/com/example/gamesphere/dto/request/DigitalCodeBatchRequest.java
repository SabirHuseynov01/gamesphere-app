package com.example.gamesphere.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DigitalCodeBatchRequest {

    @NotEmpty(message = "At least one digital code is required")
    private List<@NotBlank(message = "Digital code cannot be blank") String> codes;
}
