package com.example.gamesphere.services;

import com.example.gamesphere.dto.request.UserProfileUpdateRequest;
import com.example.gamesphere.dto.response.UserProfileResponse;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.mapper.UserMapper;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest extends ServiceTestSupport {

    @Mock UserMapper userMapper;
    @Mock UserRepository userRepository;
    @InjectMocks
    UserService userService;

    @Test
    void getCurrentProfileUsesAuthenticatedEmail() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        UserProfileResponse expected = new UserProfileResponse();
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(userMapper.toProfileResponse(user)).thenReturn(expected);

        assertThat(userService.getCurrentUserProfile()).isSameAs(expected);
    }

    @Test
    void updateProfileMapsSavesAndReturnsUpdatedUser() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        UserProfileResponse expected = new UserProfileResponse();
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toProfileResponse(user)).thenReturn(expected);

        assertThat(userService.updateProfile(request)).isSameAs(expected);
        verify(userMapper).updateProfile(user, request);
        verify(userRepository).save(user);
    }

    @Test
    void updateBalanceAddsAmountToExistingBalance() {
        User user = user(1L, "user@mail.com");
        user.setBalance(25.0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.updateBalance(1L, 15.5);

        assertThat(user.getBalance()).isEqualTo(40.5);
    }
}

