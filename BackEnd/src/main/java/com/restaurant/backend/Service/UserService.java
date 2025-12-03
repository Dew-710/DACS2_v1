package com.restaurant.backend.Service;

import com.restaurant.backend.Dto.Request.LoginRequest;
import com.restaurant.backend.Dto.Request.RegisterRequest;
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
}
