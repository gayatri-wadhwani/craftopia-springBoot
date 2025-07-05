package com.example.craftopia.Service;

import com.example.craftopia.DTO.ProductRequest;
import com.example.craftopia.DTO.ProductResponse;
import com.example.craftopia.Entity.Product;
import com.example.craftopia.Mapper.ProductDTOMapper;
import com.example.craftopia.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    public ProductRepository repo;

    public ProductResponse createProduct(ProductRequest requestDTO) {
        Product product = ProductDTOMapper.toEntity(requestDTO);
        return ProductDTOMapper.toDTO(repo.save(product));
    }

    public List<ProductResponse> getAllProducts(String category, String keyword) {
        List<Product> products;

        if (category != null && !category.isEmpty()) {
            products = repo.findByIsDeletedFalseAndCategoryContainingIgnoreCase(category);
        } else if (keyword != null && !keyword.isEmpty()) {
            products = repo.findByIsDeletedFalseAndNameContainingIgnoreCase(keyword);
        } else {
            products = repo.findByIsDeletedFalse();
        }

        return products.stream()
                .map(ProductDTOMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product product = repo.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return ProductDTOMapper.toDTO(product);
    }

    public ProductResponse updateProduct(Long id, ProductRequest requestDTO) {
        Product product = repo.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(requestDTO.getName());
        product.setDescription(requestDTO.getDescription());
        product.setPrice(requestDTO.getPrice());
        product.setCategory(requestDTO.getCategory());
        product.setImageUrl(requestDTO.getImageUrl());

        return ProductDTOMapper.toDTO(repo.save(product));
    }

    public void deleteProduct(Long id) {
        Product product = repo.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setDeleted(true);
        repo.save(product);
    }
}
