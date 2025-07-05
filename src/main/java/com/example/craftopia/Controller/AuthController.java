package com.example.craftopia.Controller;

import com.example.craftopia.DTO.AuthRequest;
import com.example.craftopia.DTO.AuthResponse;
import com.example.craftopia.DTO.RegisterRequest;
import com.example.craftopia.Service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest req) {
        return auth.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest req) {
        return auth.login(req);
    }
}
