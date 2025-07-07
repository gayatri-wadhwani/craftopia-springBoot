package com.example.craftopia.Repository;

import com.example.craftopia.Entity.Order;
import com.example.craftopia.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}

