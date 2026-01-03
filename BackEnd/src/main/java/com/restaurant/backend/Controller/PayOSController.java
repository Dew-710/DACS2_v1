package com.restaurant.backend.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.backend.Config.PayOSProperties;
import com.restaurant.backend.Dto.Request.CreatePaymentLinkRequest;
import com.restaurant.backend.Dto.Response.PaymentLinkResponse;
import com.restaurant.backend.Entity.Order;
import com.restaurant.backend.Entity.User;
import com.restaurant.backend.Repository.OrderRepository;
import com.restaurant.backend.Repository.UserRepository;
import com.restaurant.backend.Service.PayOSService;
import com.restaurant.backend.Service.PayOSWebhookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.payos.model.webhooks.Webhook;

@RestController
@RequestMapping("/api/payos")
public class PayOSController {

    private final PayOSService payOSService;
    private final PayOSWebhookService payOSWebhookService;
    private final PayOSProperties payOSProperties;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(PayOSController.class);
    private final OrderRepository orderRepository;

    public PayOSController(PayOSService payOSService,
                          PayOSWebhookService payOSWebhookService,
                          PayOSProperties payOSProperties,
                          UserRepository userRepository,
                          OrderRepository orderRepository) {

        this.payOSService = payOSService;
        this.payOSWebhookService = payOSWebhookService;
        this.payOSProperties = payOSProperties;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/link")
    public ResponseEntity<?> createLink(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Extract token from Authorization header
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            
            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Token xác thực không được cung cấp"));
            }
            
            // Extract orderId from request
            Object orderIdObj = request.get("orderId");
            if (orderIdObj == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "orderId is required"));
            }
            
            // Convert to Long
            Long orderId;
            if (orderIdObj instanceof Integer) {
                orderId = ((Integer) orderIdObj).longValue();
            } else if (orderIdObj instanceof Long) {
                orderId = (Long) orderIdObj;
            } else if (orderIdObj instanceof String) {
                orderId = Long.parseLong((String) orderIdObj);
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "orderId must be a number"));
            }
            
            return payOSService.createLink(orderId, token);
        } catch (Exception e) {
            log.error("Error creating payment link: ", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Lỗi không xác định"));
        }
    }
//
//   @PostMapping("/link")
//public ResponseEntity<?> createLink(
//        @RequestBody CreatePaymentLinkRequest req,
//        Authentication authentication
//) throws Exception {
//
//    // 1️⃣ Parse orderIds từ metadata
//    List<Long> orderIds = List.of();
//    if (req.getMetadata() != null && req.getMetadata().containsKey("orderIds")) {
//        orderIds = List.of(req.getMetadata().get("orderIds").split(","))
//                .stream()
//                .map(Long::parseLong)
//                .collect(Collectors.toList());
//    }
//
//    if (orderIds.isEmpty()) {
//        return ResponseEntity.badRequest()
//                .body("orderIds is required in metadata");
//    }
//
//    // 2️⃣ Load orders
//    List<Order> orders = orderRepository.findAllById(orderIds);
//    if (orders.isEmpty()) {
//        return ResponseEntity.badRequest()
//                .body("Orders not found");
//    }
//
//    // ✅ KHÔNG set orderCode ở đây - PayOSService sẽ auto-generate!
//    // orderCode phải là timestamp duy nhất, không phải order ID
//
//    // 4️⃣ LOG JSON GỬI PAYOS (CỰC KỲ QUAN TRỌNG)
//    ObjectMapper mapper = new ObjectMapper();
//    log.error(
//        "PAYOS REQUEST JSON = \n{}",
//        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(req)
//    );
//
//    // 5️⃣ Map user từ Authentication (optional)
//    User user = null;
//    if (authentication != null) {
//        user = userRepository
//                .findByUsername(authentication.getName())
//                .orElse(null);
//    }
//
//    // 6️⃣ Call PayOS
//    PaymentLinkResponse resp =
//            payOSService.createLink(req, user, orders);
//
//    return ResponseEntity.ok(resp);
//}


    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Webhook webhook) {
        log.info("Received PayOS webhook request");

        try {
            // Process the webhook using PayOS SDK for signature verification
            // The service will verify signature using PayOS SDK and process the payment
            String result = payOSWebhookService.processPaymentWebhook(webhook);

            log.info("Webhook processed successfully - Result: {}", result);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error processing PayOS webhook", e);
            // Return 200 OK to prevent PayOS from retrying, but log the error
            return ResponseEntity.ok("Webhook processing failed - check logs");
        }
    }
    
}
