package com.example.craftopia.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private Long productId;
    private String name;
    private String imageUrl;
    private Double price;
    private int quantity;
}
