package com.example.craftopia.Controller;

import com.example.craftopia.DTO.OrderStatusUpdateRequest;
import com.example.craftopia.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {

    @Autowired
    private OrderService service;

    // Place order
    @PreAuthorize("hasRole('BUYER')")
    @PostMapping("/place")
    public ResponseEntity<?> placeOrder() {
        try {
            return ResponseEntity.ok(service.placeOrder());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order failed: " + e.getMessage());
        }
    }

    // Get current user's orders
    @PreAuthorize("hasRole('BUYER')")
    @GetMapping("/my")
    public ResponseEntity<?> getMyOrders() {
        try {
            return ResponseEntity.ok(service.getMyOrders());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to fetch orders: " + e.getMessage());
        }
    }

    // Update order status (admin logic)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable("id") Long orderId,
                                          @RequestBody OrderStatusUpdateRequest request) {
        try {
            return ResponseEntity.ok(service.updateStatus(orderId, request.getStatus()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Status update failed: " + e.getMessage());
        }
    }

    // Get all orders (admin logic)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders() {
        try {
            return ResponseEntity.ok(service.getAllOrders());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to fetch all orders: " + e.getMessage());
        }
    }
}

