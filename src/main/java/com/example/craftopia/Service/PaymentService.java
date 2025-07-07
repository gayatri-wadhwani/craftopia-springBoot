package com.example.craftopia.Service;

import com.example.craftopia.DTO.TransactionDetails;
import com.example.craftopia.Entity.Order;
import com.example.craftopia.Entity.OrderStatus;
import com.example.craftopia.Entity.PaymentRecord;
import com.example.craftopia.Repository.OrderRepository;
import com.example.craftopia.Repository.PaymentRecordRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.UUID;

@Service
public class PaymentService {

    @Value("${app.razorpay.key}")
    private String key;

    @Value("${app.razorpay.secret}")
    private String secret;

    private final String currency = "INR";

    private final OrderRepository orderRepo;
    private final PaymentRecordRepository paymentRecordRepo;

    public PaymentService(OrderRepository orderRepo, PaymentRecordRepository paymentRecordRepo) {
        this.orderRepo = orderRepo;
        this.paymentRecordRepo = paymentRecordRepo;
    }

    public TransactionDetails createTransaction(Double amount) {
        try {
            RazorpayClient razorpayClient = new RazorpayClient(key, secret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int) (amount * 100)); // in paise
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", "txn_" + UUID.randomUUID());

            com.razorpay.Order order = razorpayClient.orders.create(orderRequest);

            return new TransactionDetails(
                    order.get("id"),
                    order.get("currency"),
                    key,
                    order.get("amount")
            );
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    public boolean verifySignatureAndSave(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature, Long internalOrderId) {
        try {
            // Step 1: Verify Signature
            String data = razorpayOrderId + "|" + razorpayPaymentId;
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            byte[] hmacData = mac.doFinal(data.getBytes());

            String generatedSignature = Base64.getEncoder().encodeToString(hmacData);

            boolean isValid = generatedSignature.equals(razorpaySignature);
            if (!isValid) return false;

            // Step 2: Fetch Order
            Order order = orderRepo.findById(internalOrderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Step 3: Save PaymentRecord
            PaymentRecord record = PaymentRecord.builder()
                    .razorpayOrderId(razorpayOrderId)
                    .razorpayPaymentId(razorpayPaymentId)
                    .razorpaySignature(razorpaySignature)
                    .amount(order.getTotalAmount())
                    .order(order)
                    .build();

            paymentRecordRepo.save(record);

            // Step 4: Update Order Status
            order.setStatus(OrderStatus.PAID);
            orderRepo.save(order);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
