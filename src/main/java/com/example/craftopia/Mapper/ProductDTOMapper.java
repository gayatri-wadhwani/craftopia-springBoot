package com.example.craftopia.Mapper;

import com.example.craftopia.DTO.ProductRequest;
import com.example.craftopia.DTO.ProductResponse;
import com.example.craftopia.Entity.Product;

public class ProductDTOMapper {

    public static Product toEntity(ProductRequest dto) {
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .imageUrl(dto.getImageUrl())
                .tags(dto.getTags())
                .style(dto.getStyle())
                .originalLanguageText(dto.getOriginalLanguageText())
                .translatedText(dto.getTranslatedText())
                .build();
    }

    public static ProductResponse toDTO(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .tags(product.getTags())
                .style(product.getStyle())
                .originalLanguageText(product.getOriginalLanguageText())
                .translatedText(product.getTranslatedText())
                .build();
    }
}
