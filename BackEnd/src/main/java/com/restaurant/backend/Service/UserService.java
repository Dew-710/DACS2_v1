package com.restaurant.backend.Service;

import com.restaurant.backend.Dto.Request.*;
import com.restaurant.backend.Entity.User;

import java.util.List;

public interface UserService {

    List<User> findAll();
    User findById(Long id);
    User create(RegisterRequest user);

    User update(Long id, User user);

    void delete(Long id);

    List<User> getAll();

    User login(LoginRequest loginRequest);

    User findByUsername (String Username);
    
    User findByEmail(String email);
    
    // Profile management
    User updateProfile(Long userId, UpdateProfileRequest request);
    
    User changePassword(Long userId, ChangePasswordRequest request);
    
    // Password reset
    void requestPasswordReset(String email);
    
    void resetPassword(ResetPasswordRequest request);
    
    boolean validateResetToken(String token);
}
