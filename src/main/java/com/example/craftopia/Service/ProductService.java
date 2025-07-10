package com.example.craftopia.Service;

import com.example.craftopia.DTO.ProductRequest;
import com.example.craftopia.DTO.ProductResponse;
import com.example.craftopia.DTO.ProductUpdateRequest;
import com.example.craftopia.Entity.Product;
import com.example.craftopia.Entity.User;
import com.example.craftopia.Mapper.ProductDTOMapper;
import com.example.craftopia.Repository.ProductRepository;
import com.example.craftopia.Repository.UserRepository;
import com.example.craftopia.Util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.opencsv.CSVReader;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    public ProductRepository repo;

    @Autowired
    public UserRepository userRepo;

    @Autowired
    public SecurityUtil securityUtil;

    public List<ProductResponse> bulkCreateProductsFromCSV(MultipartFile file) {
        User seller = securityUtil.getCurrentUser();

        List<Product> productList = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] line;
            reader.readNext(); // Skip header

            while ((line = reader.readNext()) != null) {
                Product product = Product.builder()
                        .name(line[0])
                        .description(line[1])
                        .price(Double.parseDouble(line[2]))
                        .category(line[3])
                        .imageUrl(line[4])
                        .seller(seller)
                        .build();
                productList.add(product);
            }

            List<Product> saved = repo.saveAll(productList);
            return saved.stream().map(ProductDTOMapper::toDTO).collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to process CSV: " + e.getMessage());
        }
    }

    public List<ProductResponse> getProductsBySeller() {
        User seller = securityUtil.getCurrentUser();

        List<Product> products = repo.findBySellerAndIsDeletedFalse(seller);

        return products.stream()
                .map(ProductDTOMapper::toDTO)
                .collect(Collectors.toList());
    }



    public List<ProductResponse> bulkCreateProducts(List<ProductRequest> products) {
        User seller = securityUtil.getCurrentUser();

        List<Product> productEntities = products.stream()
                .map(req -> {
                    Product product = ProductDTOMapper.toEntity(req);
                    product.setSeller(seller);
                    return product;
                })
                .collect(Collectors.toList());

        List<Product> saved = repo.saveAll(productEntities);
        return saved.stream().map(ProductDTOMapper::toDTO).collect(Collectors.toList());
    }



    public ProductResponse createProduct(ProductRequest requestDTO) {
        User seller = securityUtil.getCurrentUser();
        Product product = ProductDTOMapper.toEntity(requestDTO);
        product.setSeller(seller);
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

    public ProductResponse partialUpdateProduct(Long id, ProductUpdateRequest dto) {
        Product product = repo.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getCategory() != null) product.setCategory(dto.getCategory());
        if (dto.getImageUrl() != null) product.setImageUrl(dto.getImageUrl());

        Product updated = repo.save(product);
        return ProductDTOMapper.toDTO(updated);
    }


    public void deleteProduct(Long id) {
        Product product = repo.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setDeleted(true);
        repo.save(product);
    }
}
