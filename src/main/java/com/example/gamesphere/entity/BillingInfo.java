package com.example.gamesphere.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class BillingInfo {

    private String fullName;
    private String country;
    private String city;
    private String billingEmail;
    private String taxNumber;
}
