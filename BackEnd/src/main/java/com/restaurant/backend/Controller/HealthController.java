package com.restaurant.backend.Controller;

import com.restaurant.backend.websocket.IoTWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final IoTWebSocketHandler webSocketHandler;

    public HealthController(IoTWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "message", "Backend is running",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of(
            "message", "Test endpoint works!",
            "websocket_url", "ws://YOUR_IP:8080/ws/iot?clientType=esp32",
            "hint", "Replace YOUR_IP with your actual server IP address"
        ));
    }

    @GetMapping("/websocket/status")
    public ResponseEntity<?> websocketStatus() {
        // Get connection counts using reflection or add getter methods
        // For now, return basic info
        return ResponseEntity.ok(Map.of(
            "message", "WebSocket endpoint is available",
            "endpoint", "/ws/iot?clientType=esp32",
            "note", "Check backend logs for connection details"
        ));
    }
}
