package com.restaurant.backend.Controller;

import com.restaurant.backend.websocket.IoTWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageController {

    private final IoTWebSocketHandler ws;

    @PostMapping("/send-image/{filename}")
    public ResponseEntity<?> send(@PathVariable String filename) {
        try {
            // Load ảnh từ classpath
            ClassPathResource resource = new ClassPathResource("static/images/" + filename);
            if (!resource.exists()) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "File not found in static/images/: " + filename)
                );
            }

            try (InputStream inputStream = resource.getInputStream()) {
                byte[] data = inputStream.readAllBytes();
                ws.broadcastImageBytes(data);
            }

            return ResponseEntity.ok(
                    Map.of("message", "Image sent successfully to ESP32: " + filename)
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(
                    Map.of("message", "Error: " + ex.getMessage())
            );
        }
    }
}
