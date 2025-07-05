package com.example.craftopia.DTO;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
