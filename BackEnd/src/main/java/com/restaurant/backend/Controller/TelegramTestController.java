package com.restaurant.backend.Controller;

import com.restaurant.backend.Service.impl.TelegramBotServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
public class TelegramTestController {

    private final TelegramBotServiceImpl telegramBotService;

    @GetMapping("/test")
    public ResponseEntity<?> testTelegramBot() {
        try {
            telegramBotService.sendTestMessage();
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Test message sent! Check your Telegram.",
                            "status", "success"
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "message", "Failed to send test message: " + e.getMessage(),
                            "status", "error"
                    )
            );
        }
    }
}


