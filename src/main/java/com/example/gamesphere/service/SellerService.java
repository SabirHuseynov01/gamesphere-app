package com.example.gamesphere.service;


import com.example.gamesphere.dto.request.SellerProfileRequest;
import com.example.gamesphere.dto.response.SellerProfileResponse;
import com.example.gamesphere.entity.Role;
import com.example.gamesphere.entity.SellerProfile;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.mapper.SellerProfileMapper;
import com.example.gamesphere.repository.RoleRepository;
import com.example.gamesphere.repository.SellerProfileRepository;
import com.example.gamesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private static final String SELLER_ROLE = "ROLE_SELLER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final SellerProfileMapper sellerProfileMapper;

    @Transactional
    public SellerProfileResponse createSellerProfile(SellerProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (sellerProfileRepository.existsByUserId(user.getId())) {
            throw new BusinessException("You already have a seller profile.");
        }

        sellerProfileRepository.findByShopName(request.getShopName())
                .ifPresent(existing -> {
                    throw new BusinessException("This shop name is already taken.");
                });

        SellerProfile sellerProfile = sellerProfileMapper.toEntity(request);
        sellerProfile.setUser(user);
        sellerProfile.setApproved(false);

        SellerProfile savedProfile = sellerProfileRepository.save(sellerProfile);
        return sellerProfileMapper.toResponse(savedProfile);
    }

    public SellerProfileResponse getCurrentSellerProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SellerProfile sellerProfile = sellerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("You don't have a seller profile yet."));

        return sellerProfileMapper.toResponse(sellerProfile);
    }

    @Transactional
    public SellerProfileResponse approveSellerProfile(Long sellerProfileId) {
        SellerProfile sellerProfile = sellerProfileRepository.findById(sellerProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));

        sellerProfile.setApproved(true);

        User user = sellerProfile.getUser();
        user.setSeller(true);

        Role sellerRole = roleRepository.findByName(SELLER_ROLE)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(SELLER_ROLE).build()));
        user.getRoles().add(sellerRole);
        userRepository.save(user);

        SellerProfile approvedProfile = sellerProfileRepository.save(sellerProfile);
        return sellerProfileMapper.toResponse(approvedProfile);
    }
}
