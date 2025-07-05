package com.example.craftopia.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddToCartRequest {
    private Long userId;
    private Long productId;
    private int quantity;
}
