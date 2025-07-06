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
    public void addToCart(Long userId, AddToCartRequest req) {
        // Step 1: Ensure product exists and is not deleted
        Product product = productRepo.findById(req.getProductId())
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new RuntimeException("Product not available"));

        // Step 2: Fetch or create the user's cart
        Cart cart = cartRepo.findByUserIdAndIsDeletedFalse(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found")));
                    newCart.setItems(new ArrayList<>()); // Ensure it's initialized
                    return cartRepo.save(newCart);
                });

        // Step 3: Null safety for items list if cart already existed
        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        // Step 4: Check if item already exists in cart
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

    // Get Item Count in Cart
    // Get total quantity of items in Cart
    public int getCartItemCount(Long userId) {
        Cart cart = cartRepo.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        return cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    // Remove a product from the user's cart
    public void removeFromCart(Long userId, Long productId) {
        Cart cart = cartRepo.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1); // decrement quantity
            } else {
                cart.getItems().remove(item); // remove item completely if quantity == 1
            }
            cartRepo.save(cart);
        } else {
            throw new RuntimeException("Product not found in cart");
        }
    }

    // Clear the user's cart
    public void clearCart(Long userId) {
        Cart cart = cartRepo.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        itemRepo.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepo.save(cart);
    }
}
