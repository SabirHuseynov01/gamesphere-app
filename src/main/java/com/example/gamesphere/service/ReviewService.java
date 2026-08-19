package com.example.gamesphere.service;


import com.example.gamesphere.dto.request.ReviewCreateRequest;
import com.example.gamesphere.dto.response.PageResponse;
import com.example.gamesphere.dto.response.ReviewResponse;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.Review;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.mapper.ReviewMapper;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.repository.ReviewRepository;
import com.example.gamesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;


    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        User user = getCurrentUser();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (reviewRepository.existsByProductIdAndUserId(product.getId(), user.getId())) {
            throw new BusinessException("You have already reviewed this product.");
        }
        Review review = reviewMapper.toEntity(request);
        review.setProduct(product);
        review.setUser(user);
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toResponse(savedReview);
    }


    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findPageByProductIdAndApprovedTrue(productId, pageable);
        return PageResponse.of(reviews.map(reviewMapper::toResponse));
    }


    @Transactional(readOnly = true)
    public List<ReviewResponse> getApprovedProductReviews(Long productId) {
        return reviewRepository.findByProductIdAndApprovedTrue(productId).stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Transactional
    public ReviewResponse approveReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setApproved(true);
        return reviewMapper.toResponse(reviewRepository.save(review));
    }


    @Transactional
    public void deleteReview(Long id) {
        User user = getCurrentUser();
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        if (!review.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You can only delete your own reviews.");
        }
        reviewRepository.delete(review);
    }


    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

