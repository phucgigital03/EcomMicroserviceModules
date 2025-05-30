package com.ecommerce.order.service;

import com.ecommerce.order.clients.ProductServiceClient;
import com.ecommerce.order.clients.UserServiceClient;
import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.dto.UserResponse;
import com.ecommerce.order.model.CartItem;
import com.ecommerce.order.repository.CartItemRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    @CircuitBreaker(name = "productService", fallbackMethod = "addToCartFallBack")
    public boolean addToCart(String userId, CartItemRequest request) {
//         Look for product
        ProductResponse productResponse = productServiceClient.getProductDetails(request.getProductId());
//        System.out.println("productResponse = " + productResponse);
        if (productResponse == null) {
            return false; // Product not found
        }

        // Check if product is in stock
        if (productResponse.getStockQuantity() < request.getQuantity()) {
            return false; // Not enough stock
        }

        // Look for user
        UserResponse userResponse = userServiceClient.getUserDetails(userId);
        if (userResponse == null) {
            return false; // User not found
        }

//        User user = userOpt.get();
        // Have 2 ways to add product to cart:
        // 1. product already exists in cart
        // 2. product does not exist in cart
        CartItem existingCartItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());
        if (existingCartItem != null) {
            // Update quantity
            existingCartItem.setQuantity(existingCartItem.getQuantity() + request.getQuantity());
            existingCartItem.setPrice(BigDecimal.valueOf(1000.00));
            cartItemRepository.save(existingCartItem);
        } else {
            // Create new cart item
            CartItem newCartItem = new CartItem();
            newCartItem.setUserId(userId);
            newCartItem.setProductId(request.getProductId());
            newCartItem.setQuantity(request.getQuantity());
            newCartItem.setPrice(BigDecimal.valueOf(1000.00));
            cartItemRepository.save(newCartItem);
        }

        return true;
    }

    public boolean addToCartFallBack(String userId, CartItemRequest request, Exception exception) {
        // Fallback method for circuit breaker
        exception.printStackTrace();
        return false; // Indicate failure to add to cart
    }

    public boolean deleteItemFromCart(String userId, String productId) {
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId);

        if (cartItem != null){
            cartItemRepository.delete(cartItem);
            return true;
        }
        return false;
    }

    public List<CartItem> getCart(String userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }

}
