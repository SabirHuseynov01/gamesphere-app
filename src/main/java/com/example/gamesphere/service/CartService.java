package com.example.gamesphere.service;

import com.example.gamesphere.dto.request.AddCartItemRequest;
import com.example.gamesphere.dto.response.CartResponse;
import com.example.gamesphere.entity.Cart;
import com.example.gamesphere.entity.CartItem;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.DeliveryType;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.exception.ProductOutOfStockException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.mapper.CartMapper;
import com.example.gamesphere.repository.CartRepository;
import com.example.gamesphere.repository.ProductRepository;
import com.example.gamesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Transactional
    public CartResponse getCurrentUserCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> createNewCart(user));

        return cartMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(AddCartItemRequest request) {
        Long productId = request.getProductId();
        Integer quantity = request.getQuantity();
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.isActive() || product.isDeleted()) {
            throw new BusinessException("Product is not available.");
        }

        if (product.getDeliveryType() == DeliveryType.STORE_REDIRECT
                || product.getDeliveryType() == DeliveryType.EXTERNAL_MARKET) {
            throw new BusinessException(
                    "Redirect offers cannot be added to cart. Continue on the external store instead.");
        }

        if (product.getStockQuantity() < quantity) {
            throw new ProductOutOfStockException("The product is out of stock.");
        }

        String playerAccountId = resolvePlayerAccountId(product, request.getPlayerAccountId());

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> createNewCart(user));

        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // A row added before the id was known adopts it on the next add; only a
            // genuine switch between two different accounts is refused.
            if (existingItem.getPlayerAccountId() == null) {
                existingItem.setPlayerAccountId(playerAccountId);
            } else if (playerAccountId != null
                    && !Objects.equals(existingItem.getPlayerAccountId(), playerAccountId)) {
                throw new BusinessException(
                        "This product is already in the cart for a different player account. "
                                + "Remove it before changing the player account.");
            }

            int newQuantity = existingItem.getQuantity() + quantity;
            if (product.getStockQuantity() < newQuantity) {
                throw new ProductOutOfStockException("The requested quantity is not available.");
            }
            existingItem.setQuantity(newQuantity);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setPlayerAccountId(playerAccountId);
            item.setPriceAtAddTime(product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice());
            cart.getCartItems().add(item);
        }

        return cartMapper.toResponse(cartRepository.save(cart));
    }

    /**
     * Sets the player account id on a row that is already in the cart.
     *
     * A top-up can be added before the shopper knows their in-game id, so the
     * id has to be fillable later — re-adding the product would bump the
     * quantity instead, and the order is refused while the id is missing.
     */
    @Transactional
    public CartResponse updatePlayerAccountId(Long productId, String playerAccountId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem item = cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product is not in the cart"));

        if (!item.getProduct().isRequiresPlayerId()) {
            throw new BusinessException(item.getProduct().getName() + " does not take a player account id.");
        }

        String normalizedId = playerAccountId == null ? null : playerAccountId.trim();
        if (normalizedId == null || normalizedId.isBlank()) {
            throw new BusinessException("Player account id cannot be empty.");
        }

        item.setPlayerAccountId(normalizedId);

        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeFromCart(Long productId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        boolean removed = cart.getCartItems().removeIf(item -> item.getProduct().getId().equals(productId));
        if (!removed) {
            throw new ResourceNotFoundException("Product not found in cart");
        }

        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        cartRepository.deleteByUserId(user.getId());
    }

    private Cart createNewCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    /**
     * A shopper may not know their in-game id while still browsing, so the cart
     * accepts a top-up without one. Order placement still refuses to proceed
     * until it is supplied, which is where the id is actually needed.
     */
    private String resolvePlayerAccountId(Product product, String playerAccountId) {
        if (!product.isRequiresPlayerId()) {
            return null;
        }

        String normalizedId = playerAccountId == null ? null : playerAccountId.trim();

        return normalizedId == null || normalizedId.isBlank() ? null : normalizedId;
    }
}

