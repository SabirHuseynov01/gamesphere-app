package com.example.gamesphere.service;

import com.example.gamesphere.dto.response.WishlistResponse;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.entity.Wishlist;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.mapper.WishlistMapper;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final WishlistMapper wishlistMapper;

    @Transactional
    public WishlistResponse getCurrentUserWishlist() {
        User user = getCurrentUser();
        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
                .orElseGet(() -> createNewWishlist(user));
        return wishlistMapper.toResponse(wishlist);
    }

    @Transactional
    public WishlistResponse addToWishlist(Long productId) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
                .orElseGet(() -> createNewWishlist(user));

        if (!wishlist.getProducts().add(product)) {
            throw new BusinessException("Product is already in wishlist.");
        }

        return wishlistMapper.toResponse(wishlistRepository.save(wishlist));
    }

    @Transactional
    public WishlistResponse removeFromWishlist(Long productId) {
        User user = getCurrentUser();
        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found"));

        boolean removed = wishlist.getProducts().removeIf(product -> product.getId().equals(productId));
        if (!removed) {
            throw new ResourceNotFoundException("Product not found in wishlist");
        }

        return wishlistMapper.toResponse(wishlistRepository.save(wishlist));
    }

    @Transactional
    public void clearWishlist() {
        User user = getCurrentUser();
        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found"));
        wishlist.getProducts().clear();
        wishlistRepository.save(wishlist);
    }

    private Wishlist createNewWishlist(User user) {
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        return wishlistRepository.save(wishlist);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
