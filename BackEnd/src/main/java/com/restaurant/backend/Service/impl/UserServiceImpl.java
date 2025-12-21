package com.restaurant.backend.Service.impl;

import com.restaurant.backend.Dto.Request.LoginRequest;
import com.restaurant.backend.Dto.Request.RegisterRequest;
import com.restaurant.backend.Entity.User;
import com.restaurant.backend.Repository.UserRepository;
import com.restaurant.backend.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public User create(RegisterRequest orderDTO) {
        User user = new User();
        user.setUsername(orderDTO.getUsername());

        // Hash password using BCrypt
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(orderDTO.getPassword()));

        user.setFullName(orderDTO.getFullName());
        user.setPhone(orderDTO.getPhone());
        user.setEmail(orderDTO.getEmail());
        user.setRole(orderDTO.getRole() != null ? orderDTO.getRole() : "CUSTOMER");
        user.setStatus("ACTIVE");
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return userRepository.save(user);
    }

    @Override
    public User update(Long id, User user) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }


    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }
    @Override
    public User login(LoginRequest loginDTO) {

        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu không đúng");
        }

        return user;
    }


    @Override
    public User findByUsername(String Username) {
        return null;
    }
}
