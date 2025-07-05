package com.example.craftopia.Repository;

import com.example.craftopia.Entity.Role;
import com.example.craftopia.Entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName roleBuyer);
}
