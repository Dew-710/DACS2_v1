package com.restaurant.backend.Controller;

import com.restaurant.backend.Service.QRCodeService;
import com.restaurant.backend.Service.RestaurantTableService;
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
    private final QRCodeService qrCodeService;
    private final RestaurantTableService tableService;

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

    @PostMapping("/send-qr-code/{tableId}")
    public ResponseEntity<?> sendQRCodeToESP32(@PathVariable Long tableId) {
        try {
            var table = tableService.findById(tableId);
            if (table == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Table not found")
                );
            }

            if (table.getQrCode() == null || table.getQrCode().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Table does not have a QR code. Please generate one first.")
                );
            }

            // Generate URL for QR code
            String frontendUrl = System.getenv("FRONTEND_URL");
            if (frontendUrl == null || frontendUrl.isEmpty()) {
                frontendUrl = "http://localhost:3000";
            }
            String qrUrl = frontendUrl + "/menu/" + table.getQrCode();
            
            // Generate QR code image (128x128 for ESP32 display)
            byte[] qrImageBytes = qrCodeService.generateQRCodeImageBytes(qrUrl, 128, 128);
            
            // Send to ESP32 via WebSocket
            ws.broadcastImageBytes(qrImageBytes);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "QR code sent to ESP32 successfully",
                            "tableId", tableId,
                            "tableName", table.getTableName(),
                            "qrCode", table.getQrCode(),
                            "qrUrl", qrUrl
                    )
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(
                    Map.of("message", "Error: " + ex.getMessage())
            );
        }
    }

    @GetMapping("/qr-code/{tableId}/image")
    public ResponseEntity<?> getQRCodeImage(@PathVariable Long tableId) {
        try {
            var table = tableService.findById(tableId);
            if (table == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Table not found")
                );
            }

            if (table.getQrCode() == null || table.getQrCode().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Table does not have a QR code")
                );
            }

            // Generate URL for QR code
            String frontendUrl = System.getenv("FRONTEND_URL");
            if (frontendUrl == null || frontendUrl.isEmpty()) {
                frontendUrl = "http://localhost:3000";
            }
            String qrUrl = frontendUrl + "/menu/" + table.getQrCode();
            
            // Generate QR code image
            byte[] qrImageBytes = qrCodeService.generateQRCodeImageBytes(qrUrl, 256, 256);

            return ResponseEntity.ok()
                    .header("Content-Type", "image/png")
                    .body(qrImageBytes);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(
                    Map.of("message", "Error: " + ex.getMessage())
            );
        }
    }
}
