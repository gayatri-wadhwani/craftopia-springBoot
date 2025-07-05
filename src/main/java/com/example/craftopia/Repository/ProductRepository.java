package com.example.craftopia.Repository;

import com.example.craftopia.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByIsDeletedFalse();

    List<Product> findByIsDeletedFalseAndCategoryContainingIgnoreCase(String category);

    List<Product> findByIsDeletedFalseAndNameContainingIgnoreCase(String keyword);
}
