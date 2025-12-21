package com.restaurant.backend.Controller;

import com.restaurant.backend.Dto.Request.LoginRequest;
import com.restaurant.backend.Dto.Request.RegisterRequest;
import com.restaurant.backend.Entity.User;
import com.restaurant.backend.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account in the system")
    public ResponseEntity<?> register(@RequestBody RegisterRequest user) {
        User created = userService.create(user);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Register successfully",
                        "user", created
                )
        );
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        User user = userService.login(loginRequest);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Login successful",
                        "user", user
                )
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(userService.getAll());
    }
}
