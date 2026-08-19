package com.example.gamesphere.service;

import com.example.gamesphere.dto.request.UserProfileUpdateRequest;
import com.example.gamesphere.dto.response.UserProfileResponse;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.mapper.UserMapper;
import com.example.gamesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserProfileResponse getCurrentUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toProfileResponse(user);
    }


    @Transactional
    public UserProfileResponse updateProfile(UserProfileUpdateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userMapper.updateProfile(user, request);
        User savedUser = userRepository.save(user);
        return userMapper.toProfileResponse(savedUser);
    }


    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toProfileResponse(user);
    }

    public UserProfileResponse getUserProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toProfileResponse(user);
    }


    @Transactional
    public UserProfileResponse updateBalance(Long userId, Double amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setBalance(user.getBalance() + amount);
        user = userRepository.save(user);
        return userMapper.toProfileResponse(user);
    }


    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }
}
