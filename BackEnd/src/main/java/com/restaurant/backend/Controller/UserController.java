package com.restaurant.backend.Controller;

import com.restaurant.backend.Dto.Request.*;
import com.restaurant.backend.Entity.User;
import com.restaurant.backend.Service.UserService;
import com.restaurant.backend.Service.JwtService;
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
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
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
        
        // Generate JWT token
        String token = jwtService.generateToken(user.getUsername());

        return ResponseEntity.ok(
                Map.of(
                        "message", "Login successful",
                        "user", user,
                        "token", token
                )
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a user by their ID")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(
                Map.of(
                        "message", "User retrieved successfully",
                        "user", user
                )
        );
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Update user", description = "Update user information")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody User user) {
        User updated = userService.update(id, user);
        return ResponseEntity.ok(
                Map.of(
                        "message", "User updated successfully",
                        "user", updated
                )
        );
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete user", description = "Delete a user by ID")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(
                Map.of("message", "User deleted successfully")
        );
    }

    @PutMapping("/profile/{id}")
    @Operation(summary = "Update user profile", description = "Update personal information for a user")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody UpdateProfileRequest request) {
        try {
            User updatedUser = userService.updateProfile(id, request);
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Cập nhật thông tin cá nhân thành công",
                            "user", updatedUser
                    )
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    @PutMapping("/change-password/{id}")
    @Operation(summary = "Change password", description = "Change user password (requires current password)")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(id, request);
            return ResponseEntity.ok(
                    Map.of("message", "Đổi mật khẩu thành công")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Send password reset email to user")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            userService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok(
                    Map.of("message", "Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư của bạn.")
            );
        } catch (RuntimeException e) {
            // Return success message even if user not found (security best practice)
            return ResponseEntity.ok(
                    Map.of("message", "Nếu email tồn tại trong hệ thống, bạn sẽ nhận được email đặt lại mật khẩu.")
            );
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using token from email")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request);
            return ResponseEntity.ok(
                    Map.of("message", "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập với mật khẩu mới.")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    @GetMapping("/validate-reset-token")
    @Operation(summary = "Validate reset token", description = "Check if password reset token is valid")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        boolean isValid = userService.validateResetToken(token);
        return ResponseEntity.ok(
                Map.of(
                        "valid", isValid,
                        "message", isValid ? "Token hợp lệ" : "Token không hợp lệ hoặc đã hết hạn"
                )
        );
    }
}
