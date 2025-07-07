package com.example.craftopia.Mapper;

import com.example.craftopia.DTO.OrderItemResponse;
import com.example.craftopia.DTO.OrderResponse;
import com.example.craftopia.Entity.Order;
import com.example.craftopia.Entity.OrderItem;

import java.util.Collections;
import java.util.stream.Collectors;

public class OrderDTOMapper {
        public static OrderResponse toDTO(Order order) {
            return OrderResponse.builder()
                    .id(order.getId())
                    .totalAmount(order.getTotalAmount())
                    .status(order.getStatus().toString())
                    .createdAt(order.getCreatedAt())
                    .items(order.getItems() != null ? order.getItems().stream()
                            .map(OrderDTOMapper::toOrderItemDTO)
                            .collect(Collectors.toList()) : Collections.emptyList())
                    .build();
        }

        public static OrderItemResponse toOrderItemDTO(OrderItem item) {
            return OrderItemResponse.builder()
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .build();
        }
}
