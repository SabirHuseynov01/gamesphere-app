package com.example.gamesphere.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private String phoneNumber;
    private String address;
    private Double balance;
    private boolean isSeller;
    /**
     * Role names ("ROLE_ADMIN", ...). The UI needs them to decide which panels
     * to render; every endpoint behind them is still guarded by @PreAuthorize,
     * so a forged value in the browser buys nothing.
     */
    private Set<String> roles = new HashSet<>();
}
