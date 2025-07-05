package com.example.craftopia.Repository;

import com.example.craftopia.Entity.Cart;
import com.example.craftopia.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserIdAndIsDeletedFalse(Long userId);
}
