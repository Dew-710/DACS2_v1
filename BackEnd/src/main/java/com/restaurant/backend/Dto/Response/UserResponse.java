package com.restaurant.backend.Dto.Response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Integer id;
    private String username;
    private String fullName;
    private String phone;
    private String email;
    private String role;
    private LocalDateTime createdAt;
}
