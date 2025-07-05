package com.example.craftopia.Service;

import com.example.craftopia.DTO.AuthRequest;
import com.example.craftopia.DTO.RegisterRequest;
import com.example.craftopia.Entity.Role;
import com.example.craftopia.Entity.RoleName;
import com.example.craftopia.Entity.User;
import com.example.craftopia.Repository.RoleRepository;
import com.example.craftopia.Repository.UserRepository;
import com.example.craftopia.Security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthService(UserRepository ur, RoleRepository rr, PasswordEncoder pe, JwtUtil jwt) {
        this.userRepo = ur; this.roleRepo = rr; this.encoder = pe; this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User u = new User();
        u.setEmail(req.getEmail());
        u.setName(req.getName());
        u.setPassword(encoder.encode(req.getPassword()));

        RoleName roleName;
        if (req.getRole() == null || req.getRole().isBlank()) {
            // Default to buyer
            roleName = RoleName.ROLE_BUYER;
        } else {
            try {
                // Convert string to enum safely
                roleName = RoleName.valueOf("ROLE_" + req.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role: " + req.getRole());
            }
        }

        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName.name()));

        u.setRoles(Set.of(role));
        userRepo.save(u);

        String token = jwt.generateToken(u.getEmail());
        return new AuthResponse(token);
    }

    public AuthResponse login(AuthRequest req) {
        User u = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!encoder.matches(req.getPassword(), u.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return new AuthResponse(jwt.generateToken(u.getEmail()));
    }
}

