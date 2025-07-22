package com.example.craftopia.DTO;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIGeneratedProductDetails {
    private String detectedLanguage;
    private String englishTitle;
    private String englishDescription;
    private List<String> tags;
    private String category;
    private String subCategory;
    private String suggestedPrice;
    private List<String> materials;
    private List<String> colors;
    private String size;
    private String occasion;
}

