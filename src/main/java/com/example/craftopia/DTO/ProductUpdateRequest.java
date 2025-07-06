package com.example.craftopia.DTO;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductUpdateRequest {
    private String name;
    private String description;
    private Double price;
    private String category;
    private String imageUrl;
}
