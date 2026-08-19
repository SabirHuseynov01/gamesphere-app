package com.example.gamesphere.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seller_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfile extends BaseEntity{

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String shopName;

    @Column(length = 1000)
    private String bio;

    private String contactEmail;

    private String phoneNumber;

    @Builder.Default
    private boolean isApproved = false;
}
