package com.example.gamesphere.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseEntity{

    @Column(nullable = false, unique = true)
    private String name; // ROLE_USER, ROLE_ADMIN, ROLE_SELLER
}
