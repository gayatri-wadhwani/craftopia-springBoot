package com.example.craftopia.DTO;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String imageUrl;
    private String sellerEmail;
    private List<String> tags;
    private String style;
    private String originalLanguageText;
    private String translatedText;
}
