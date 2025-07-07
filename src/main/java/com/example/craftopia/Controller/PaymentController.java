package com.example.craftopia.Controller;

import com.example.craftopia.DTO.PaymentVerificationRequest;
import com.example.craftopia.Entity.OrderStatus;
import com.example.craftopia.DTO.TransactionDetails;
import com.example.craftopia.Service.OrderService;
import com.example.craftopia.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    // Razorpay Order Creation
    @PreAuthorize("hasRole('BUYER')")
    @GetMapping("/{amount}")
    public ResponseEntity<TransactionDetails> createTransaction(@PathVariable("amount") Double amount) {
        TransactionDetails transaction = paymentService.createTransaction(amount);
        return ResponseEntity.ok(transaction);
    }

    // Payment Verification Endpoint
    @PostMapping("/verify")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        boolean isValid = paymentService.verifySignatureAndSave(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                request.getOrderId()
        );

        if (isValid) {
            return ResponseEntity.ok("Payment verified and recorded. Order marked as PAID.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment verification failed.");
        }
    }

}
