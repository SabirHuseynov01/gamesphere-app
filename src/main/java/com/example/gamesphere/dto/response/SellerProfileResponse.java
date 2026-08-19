package com.example.gamesphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfileResponse {

    private Long id;
    private Long userId;
    private String username;
    private String shopName;
    private String bio;
    private String contactEmail;
    private String phoneNumber;
    private boolean isApproved;
    private LocalDateTime createdAt;
}
