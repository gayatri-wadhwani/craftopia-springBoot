package com.example.craftopia.Service;

import com.example.craftopia.DTO.AddToCartRequest;
import com.example.craftopia.DTO.CartItemResponse;
import com.example.craftopia.Entity.Cart;
import com.example.craftopia.Entity.CartItem;
import com.example.craftopia.Entity.Product;
import com.example.craftopia.Entity.User;
import com.example.craftopia.Mapper.CartDTOMapper;
import com.example.craftopia.Repository.CartItemRepository;
import com.example.craftopia.Repository.CartRepository;
import com.example.craftopia.Repository.ProductRepository;
import com.example.craftopia.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired private CartRepository cartRepo;
    @Autowired private CartItemRepository itemRepo;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepo;

    // Get all cart items for a user
    public List<CartItemResponse> getCartItems(Long userId) {
        Cart cart = cartRepo.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        return cart.getItems().stream()
                .map(item -> {
                    Product product = productRepo.findById(item.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found"));
                    return CartDTOMapper.toDTO(item, product);
                })
                .collect(Collectors.toList());
    }

    // Add item to user's cart
    public void addToCart(AddToCartRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepo.findByUserAndIsDeletedFalse(user)
                .orElseGet(() -> cartRepo.save(Cart.builder().user(user).items(new ArrayList<>()).build()));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(req.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + req.getQuantity());
        } else {
            CartItem item = CartItem.builder()
                    .productId(req.getProductId())
                    .quantity(req.getQuantity())
                    .cart(cart)
                    .build();
            cart.getItems().add(item);
        }

        cartRepo.save(cart);
    }

    // Remove a product from the user's cart
    public void removeFromCart(Long userId, Long productId) {
        Cart cart = cartRepo.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.setItems(cart.getItems().stream()
                .filter(item -> !item.getProductId().equals(productId))
                .collect(Collectors.toList()));

        cartRepo.save(cart);
    }

    // Clear the user's cart
    public void clearCart(Long userId) {
        Cart cart = cartRepo.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().clear();
        cartRepo.save(cart);
    }
}
