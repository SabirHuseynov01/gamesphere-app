package com.example.gamesphere.services;

import com.example.gamesphere.dto.request.ReviewCreateRequest;
import com.example.gamesphere.dto.response.ReviewResponse;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.Review;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.mapper.ReviewMapper;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.repository.ReviewRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest extends ServiceTestSupport {

    @Mock ReviewRepository reviewRepository;
    @Mock ReviewMapper reviewMapper;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @InjectMocks ReviewService reviewService;

    @Test
    void createReviewConnectsCurrentUserAndProduct() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        Product product = withId(new Product(), 5L);
        ReviewCreateRequest request = new ReviewCreateRequest(5L, 4.5, "Excellent");
        Review review = new Review();
        ReviewResponse expected = new ReviewResponse();
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByProductIdAndUserId(5L, 1L)).thenReturn(false);
        when(reviewMapper.toEntity(request)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(review);
        when(reviewMapper.toResponse(review)).thenReturn(expected);

        assertThat(reviewService.createReview(request)).isSameAs(expected);
        assertThat(review.getUser()).isSameAs(user);
        assertThat(review.getProduct()).isSameAs(product);
    }

    @Test
    void secondReviewForSameProductIsRejected() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        Product product = withId(new Product(), 5L);
        ReviewCreateRequest request = new ReviewCreateRequest(5L, 4.0, "Good");
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByProductIdAndUserId(5L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already reviewed");
    }

    @Test
    void ownerCanDeleteOwnReview() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        Review review = new Review();
        review.setUser(user);
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(review));

        reviewService.deleteReview(3L);

        verify(reviewRepository).delete(review);
    }
}

