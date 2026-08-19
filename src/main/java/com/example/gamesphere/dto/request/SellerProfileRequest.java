package com.example.gamesphere.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfileRequest {

    @NotBlank
    @Size(max = 100)
    private String shopName;

    @Size(max = 1000)
    private String bio;

    @NotBlank
    @Email
    private String contactEmail;

    private String phoneNumber;
}
