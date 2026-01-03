package com.restaurant.backend.Service;

import org.springframework.http.ResponseEntity;

public interface PayOSService {
    ResponseEntity<?> createLink(Long orderId, String token) throws Exception;
}
