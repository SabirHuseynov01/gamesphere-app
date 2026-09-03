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
import com.example.gamesphere.repository.GameRepository;
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
    private final GameRepository gameRepository;


    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        User user = getCurrentUser();
        Product product;
        com.example.gamesphere.entity.Game game;

        if (request.getGameId() != null) {
            game = gameRepository.findById(request.getGameId())
                    .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
            product = request.getProductId() == null
                    ? productRepository.findFirstByGameIdAndIsDeletedFalseOrderByIdAsc(game.getId())
                    .orElseThrow(() -> new BusinessException("A game needs at least one offer before it can be reviewed."))
                    : productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            if (product.getGame() == null || !product.getGame().getId().equals(game.getId())) {
                throw new BusinessException("Product does not belong to the selected game.");
            }
            if (reviewRepository.existsByGameIdAndUserId(game.getId(), user.getId())) {
                throw new BusinessException("You have already reviewed this game.");
            }
        } else {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            game = product.getGame();
            if (game != null && reviewRepository.existsByGameIdAndUserId(game.getId(), user.getId())) {
                throw new BusinessException("You have already reviewed this game.");
            }
            if (game == null && reviewRepository.existsByProductIdAndUserId(product.getId(), user.getId())) {
                throw new BusinessException("You have already reviewed this product.");
            }
        }
        Review review = reviewMapper.toEntity(request);
        review.setProduct(product);
        review.setGame(game);
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

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getGameReviews(Long gameId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findPageByGameIdAndApprovedTrue(gameId, pageable);
        return PageResponse.of(reviews.map(reviewMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getApprovedGameReviews(Long gameId) {
        return reviewRepository.findByGameIdAndApprovedTrue(gameId).stream()
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

