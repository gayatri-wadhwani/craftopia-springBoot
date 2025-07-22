package com.example.craftopia.Controller;

import com.example.craftopia.DTO.AddToCartRequest;
import com.example.craftopia.Service.CartService;
import com.example.craftopia.Util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@PreAuthorize("hasRole('BUYER')")
public class CartController {

    @Autowired
    private CartService service;

    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<?> getCart() {
        try {
            Long userId = securityUtil.getCurrentUserId();
            return ResponseEntity.ok(service.getCartItems(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart not found: " + e.getMessage());
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> getCartItemCount() {
        try {
            Long userId = securityUtil.getCurrentUserId();
            return ResponseEntity.ok(service.getCartItemCount(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart not found: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest req) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            service.addToCart(userId, req);
            return ResponseEntity.ok(Map.of("message", "Item added to cart"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to add item: " + e.getMessage());
        }
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable("productId") Long productId) {
        try {
            Long userId = securityUtil.getCurrentUserId();
            service.removeFromCart(userId, productId);
            return ResponseEntity.ok(Map.of("message", "Item removed from cart"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to remove item: " + e.getMessage());
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart() {
        try {
            Long userId = securityUtil.getCurrentUserId();
            service.clearCart(userId);
            return ResponseEntity.ok(Map.of("Message", "Cart cleared"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to clear cart: " + e.getMessage());
        }
    }
}
