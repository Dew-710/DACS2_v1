package com.restaurant.backend.Dto.Request;

import lombok.Data;
import org.springframework.modulith.NamedInterface;

@NamedInterface
@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private String phone;
    private String email;
    private String role; // ADMIN / STAFF / CUSTOMER
}
