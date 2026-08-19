package com.example.gamesphere.services;

import com.example.gamesphere.dto.response.WishlistResponse;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.entity.Wishlist;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.mapper.WishlistMapper;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.repository.WishlistRepository;
import com.example.gamesphere.service.WishlistService;
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
class WishlistServiceTest extends ServiceTestSupport {

    @Mock WishlistRepository wishlistRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @Mock WishlistMapper wishlistMapper;
    @InjectMocks
    WishlistService wishlistService;

    @Test
    void addToWishlistPersistsProductForCurrentUser() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        Product product = withId(new Product(), 4L);
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        WishlistResponse expected = new WishlistResponse();

        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(4L)).thenReturn(Optional.of(product));
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));
        when(wishlistRepository.save(wishlist)).thenReturn(wishlist);
        when(wishlistMapper.toResponse(wishlist)).thenReturn(expected);

        assertThat(wishlistService.addToWishlist(4L)).isSameAs(expected);
        assertThat(wishlist.getProducts()).containsExactly(product);
    }

    @Test
    void duplicateWishlistProductIsRejected() {
        authenticate("user@mail.com");
        User user = user(1L, "user@mail.com");
        Product product = withId(new Product(), 4L);
        Wishlist wishlist = new Wishlist();
        wishlist.getProducts().add(product);
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(4L)).thenReturn(Optional.of(product));
        when(wishlistRepository.findByUserId(1L)).thenReturn(Optional.of(wishlist));

        assertThatThrownBy(() -> wishlistService.addToWishlist(4L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already in wishlist");
    }
}
