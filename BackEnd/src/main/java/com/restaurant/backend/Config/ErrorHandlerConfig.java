package com.restaurant.backend.Config;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@ControllerAdvice
@RestController
public class ErrorHandlerConfig implements ErrorController {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        // Log the error for debugging
        System.err.println("⚠️ Invalid HTTP request detected:");
        System.err.println("   Error: " + ex.getMessage());
        System.err.println("   This usually means ESP32 or client sent malformed HTTP request");
        
        // Return a helpful error message
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            Map.of(
                "error", "Invalid HTTP request",
                "message", "The HTTP request format is invalid. Please check your client code.",
                "hint", "For WebSocket connections, use: ws://IP:8080/ws/iot?clientType=esp32"
            )
        );
    }

    @RequestMapping("/error")
    public ResponseEntity<?> handleError() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            Map.of(
                "error", "Invalid request",
                "message", "Please check your request format"
            )
        );
    }
}
