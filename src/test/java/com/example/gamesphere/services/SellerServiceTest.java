package com.example.gamesphere.services;

import com.example.gamesphere.dto.request.SellerProfileRequest;
import com.example.gamesphere.dto.response.SellerProfileResponse;
import com.example.gamesphere.entity.Role;
import com.example.gamesphere.entity.SellerProfile;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.mapper.SellerProfileMapper;
import com.example.gamesphere.repository.RoleRepository;
import com.example.gamesphere.repository.SellerProfileRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.service.SellerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest extends ServiceTestSupport {

    @Mock
    UserRepository userRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    SellerProfileRepository sellerProfileRepository;
    @Mock
    SellerProfileMapper sellerProfileMapper;
    @InjectMocks
    SellerService sellerService;

    @Test
    void createSellerProfileStartsPendingWithoutGrantingSellerRole() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        SellerProfileRequest request = new SellerProfileRequest();
        request.setShopName("Sabir Games");
        SellerProfile profile = new SellerProfile();
        SellerProfileResponse expected = new SellerProfileResponse();

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(sellerProfileRepository.existsByUserId(1L)).thenReturn(false);
        when(sellerProfileRepository.findByShopName("Sabir Games")).thenReturn(Optional.empty());
        when(sellerProfileMapper.toEntity(request)).thenReturn(profile);
        when(sellerProfileRepository.save(profile)).thenReturn(profile);
        when(sellerProfileMapper.toResponse(profile)).thenReturn(expected);

        assertThat(sellerService.createSellerProfile(request)).isSameAs(expected);
        assertThat(profile.getUser()).isSameAs(user);
        assertThat(profile.isApproved()).isFalse();
        assertThat(user.isSeller()).isFalse();
    }

    @Test
    void duplicateSellerProfileIsRejected() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(sellerProfileRepository.existsByUserId(1L)).thenReturn(true);

        assertThatThrownBy(() -> sellerService.createSellerProfile(new SellerProfileRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already have");
    }

    @Test
    void approveSellerProfileGrantsSellerFlagAndRole() {
        User user = user(1L, "user@mail.com");
        SellerProfile profile = new SellerProfile();
        profile.setUser(user);
        Role sellerRole = Role.builder().name("ROLE_SELLER").build();
        SellerProfileResponse expected = new SellerProfileResponse();

        when(sellerProfileRepository.findById(7L)).thenReturn(Optional.of(profile));
        when(roleRepository.findByName("ROLE_SELLER")).thenReturn(Optional.of(sellerRole));
        when(sellerProfileRepository.save(profile)).thenReturn(profile);
        when(sellerProfileMapper.toResponse(profile)).thenReturn(expected);

        assertThat(sellerService.approveSellerProfile(7L)).isSameAs(expected);
        assertThat(profile.isApproved()).isTrue();
        assertThat(user.isSeller()).isTrue();
        assertThat(user.getRoles()).contains(sellerRole);
    }
}

