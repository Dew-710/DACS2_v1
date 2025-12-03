package com.restaurant.backend.Controller;

import com.restaurant.backend.Entity.Payment;
import com.restaurant.backend.Service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Create new payment
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Payment payment) {
        Payment created = paymentService.create(payment);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Payment created successfully",
                        "payment", created
                )
        );
    }

    // Process payment for order
    @PostMapping("/process/{orderId}")
    public ResponseEntity<?> processPayment(@PathVariable Long orderId, @RequestBody Payment payment) {
        Payment processed = paymentService.processPayment(orderId, payment);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Payment processed successfully",
                        "payment", processed
                )
        );
    }

    // Get all payments
    @GetMapping("/list")
    public ResponseEntity<?> getAll() {
        List<Payment> payments = paymentService.getAll();
        return ResponseEntity.ok(
                Map.of(
                        "message", "Payments retrieved successfully",
                        "payments", payments
                )
        );
    }

    // Get payment by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Payment payment = paymentService.getById(id);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Payment retrieved successfully",
                        "payment", payment
                )
        );
    }

    // Get payment by order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getByOrder(@PathVariable Long orderId) {
        Payment payment = paymentService.getPaymentByOrder(orderId);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Payment retrieved successfully",
                        "payment", payment
                )
        );
    }

    // Get payments by status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(@PathVariable String status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Payments retrieved successfully",
                        "payments", payments
                )
        );
    }

    // Get payments by method
    @GetMapping("/method/{method}")
    public ResponseEntity<?> getByMethod(@PathVariable String method) {
        List<Payment> payments = paymentService.getPaymentsByMethod(method);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Payments retrieved successfully",
                        "payments", payments
                )
        );
    }

    // Update payment
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Payment payment) {
        Payment updated = paymentService.update(id, payment);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Payment updated successfully",
                        "payment", updated
                )
        );
    }

    // Refund payment
    @PutMapping("/{paymentId}/refund")
    public ResponseEntity<?> refundPayment(@PathVariable Long paymentId,
                                         @RequestParam java.math.BigDecimal amount) {
        Payment refunded = paymentService.refundPayment(paymentId, amount);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Payment refunded successfully",
                        "payment", refunded
                )
        );
    }

    // Delete payment
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        paymentService.delete(id);
        return ResponseEntity.ok(
                Map.of("message", "Payment deleted successfully")
        );
    }
}
