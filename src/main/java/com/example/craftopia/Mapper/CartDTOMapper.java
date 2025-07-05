package com.example.craftopia.Mapper;

import com.example.craftopia.DTO.CartItemResponse;
import com.example.craftopia.Entity.CartItem;
import com.example.craftopia.Entity.Product;

public class CartDTOMapper {

    public static CartItemResponse toDTO(CartItem item, Product product) {
        return CartItemResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .imageUrl(product.getImageUrl())
                .price(product.getPrice())
                .quantity(item.getQuantity())
                .build();
    }
}
