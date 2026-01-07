package com.restaurant.backend.Config;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

@ControllerAdvice
@RestController
public class ErrorHandlerConfig implements ErrorController {
    // Error handler with detailed logging

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        System.err.println("❌ JSON parsing error:");
        System.err.println("   Error: " + ex.getMessage());
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            Map.of(
                "error", "Invalid JSON format",
                "message", "Failed to parse request body",
                "details", ex.getMostSpecificCause().getMessage()
            )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        System.err.println("❌ Validation error:");
        System.err.println("   Error: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            Map.of(
                "error", "Validation failed",
                "message", "Request validation failed",
                "details", ex.getBindingResult().getAllErrors()
            )
        );
    }

    @RequestMapping("/error")
    public ResponseEntity<?> handleError(jakarta.servlet.http.HttpServletRequest request) {
        // Log the error details
        System.err.println("❌ Error endpoint hit:");
        System.err.println("   URI: " + request.getRequestURI());
        System.err.println("   Method: " + request.getMethod());
        System.err.println("   Error Message: " + request.getAttribute("jakarta.servlet.error.message"));
        System.err.println("   Error Exception: " + request.getAttribute("jakarta.servlet.error.exception"));
        System.err.println("   Status Code: " + request.getAttribute("jakarta.servlet.error.status_code"));
        
        Exception ex = (Exception) request.getAttribute("jakarta.servlet.error.exception");
        if (ex != null) {
            System.err.println("   Exception details:");
            ex.printStackTrace();
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            Map.of(
                "error", "Invalid request",
                "message", "Please check your request format",
                "details", request.getAttribute("jakarta.servlet.error.message") != null ? 
                    request.getAttribute("jakarta.servlet.error.message").toString() : "No details available"
            )
        );
    }
}
