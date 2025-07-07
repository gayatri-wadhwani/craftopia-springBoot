package com.example.craftopia.Service;

import com.example.craftopia.DTO.OrderResponse;
import com.example.craftopia.Entity.*;
import com.example.craftopia.Mapper.OrderDTOMapper;
import com.example.craftopia.Repository.CartRepository;
import com.example.craftopia.Repository.OrderRepository;
import com.example.craftopia.Repository.ProductRepository;
import com.example.craftopia.Repository.UserRepository;
import com.example.craftopia.Util.SecurityUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepo;
    @Autowired private CartRepository cartRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private SecurityUtil securityUtil;

    // Place Order
    @Transactional
    public OrderResponse placeOrder() {
        Long userId = securityUtil.getCurrentUserId();
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepo.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty())
            throw new RuntimeException("Cart is empty");

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0;

        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepo.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();

            orderItems.add(orderItem);
            totalAmount += product.getPrice() * cartItem.getQuantity();
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        order = orderRepo.save(order);

        for (OrderItem item : orderItems) item.setOrder(order);
        order.setItems(orderItems);
        orderRepo.save(order);

        cart.getItems().clear(); // clear cart after order
        cartRepo.save(cart);

        return OrderDTOMapper.toDTO(order);
    }

    // Get user's orders
    public List<OrderResponse> getMyOrders() {
        Long userId = securityUtil.getCurrentUserId();
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<Order> orders = orderRepo.findByUser(user);
        return orders.stream().map(OrderDTOMapper::toDTO).toList();
    }

    // Update status (admin logic)
    public OrderResponse updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);
        return OrderDTOMapper.toDTO(orderRepo.save(order));
    }

    // Get All Orders (admin logic)
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepo.findAll();
        return orders.stream().map(OrderDTOMapper::toDTO).toList();
    }
}

