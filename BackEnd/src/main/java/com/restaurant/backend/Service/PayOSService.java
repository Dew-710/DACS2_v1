package com.restaurant.backend.Service;

import org.springframework.http.ResponseEntity;
import vn.payos.model.webhooks.Webhook;

public interface PayOSService {
    ResponseEntity<?> createLink(Long orderId, String token) throws Exception;
    // ✅ Webhook handler KHÔNG throw exception - luôn trả về OK cho PayOS
    ResponseEntity<String> handleWebhook(Webhook webhook)throws Exception;
}
