package com.restaurant.backend.Controller;

import com.restaurant.backend.Dto.Request.PayosWebhookRequest;
import com.restaurant.backend.Dto.Request.SepayPaymentRequest;
import com.restaurant.backend.Dto.Response.PaymentLinkResponse;
import com.restaurant.backend.Dto.Response.SepayPaymentResponse;
import com.restaurant.backend.Dto.Response.SepayPaymentStatusResponse;
import com.restaurant.backend.Entity.Payment;
import com.restaurant.backend.Service.PaymentService;
import com.restaurant.backend.Service.QRCodeService;
import com.restaurant.backend.Service.SepayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final SepayService sepayService;
    private final QRCodeService qrCodeService;

    public PaymentController(PaymentService paymentService, SepayService sepayService, QRCodeService qrCodeService) {
        this.paymentService = paymentService;
        this.sepayService = sepayService;
        this.qrCodeService = qrCodeService;
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

    // Create payment link for multiple orders (simple implementation)
    @PostMapping("/link")
    public ResponseEntity<?> createPaymentLink(@RequestHeader("Authorization") String token,
                                               @RequestBody List<Long> orderIds) {
        PaymentLinkResponse response = paymentService.createPaymentLink(token, orderIds);
        return ResponseEntity.ok(Map.of("message", "Payment link created", "data", response));
    }

    // Webhook endpoint for PayOS (simple implementation)
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody PayosWebhookRequest webhook) throws Exception {
        paymentService.handleWebhook(webhook);
        return ResponseEntity.ok(Map.of("message", "Webhook processed"));
    }

    // ========== SEPAY PAYMENT ENDPOINTS ==========

    /**
     * Create Sepay payment
     * POST /api/payments/sepay/create
     */
    @PostMapping("/sepay/create")
    public ResponseEntity<?> createSepayPayment(@RequestBody SepayPaymentRequest request) {
        try {
            SepayPaymentResponse response = sepayService.createPayment(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Sepay payment created successfully",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage() != null ? e.getMessage() : "Failed to create Sepay payment"
            ));
        }
    }

    /**
     * Get Sepay payment status
     * GET /api/payments/sepay/status/{transactionId}
     */
    @GetMapping("/sepay/status/{transactionId}")
    public ResponseEntity<?> getSepayPaymentStatus(@PathVariable String transactionId) {
        try {
            SepayPaymentStatusResponse response = sepayService.getPaymentStatus(transactionId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment status retrieved successfully",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage() != null ? e.getMessage() : "Failed to get payment status"
            ));
        }
    }

    /**
     * Cancel Sepay payment
     * POST /api/payments/sepay/cancel/{transactionId}
     */
    @PostMapping("/sepay/cancel/{transactionId}")
    public ResponseEntity<?> cancelSepayPayment(@PathVariable String transactionId) {
        try {
            boolean cancelled = sepayService.cancelPayment(transactionId);
            return ResponseEntity.ok(Map.of(
                    "success", cancelled,
                    "message", cancelled ? "Payment cancelled successfully" : "Failed to cancel payment"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage() != null ? e.getMessage() : "Failed to cancel payment"
            ));
        }
    }

    /**
     * Get Sepay payment QR code image
     * GET /api/payments/sepay/qr/{transactionId}
     */
    @GetMapping("/sepay/qr/{transactionId}")
    public ResponseEntity<?> getSepayQRCode(@PathVariable String transactionId) {
        try {
            // Get payment data from service (includes account info)
            SepayPaymentResponse paymentData = sepayService.getPaymentData(transactionId);
            
            if (paymentData == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Payment not found for transaction: " + transactionId
                ));
            }
            
            // Build QR code content for bank transfer
            // Format: STK: {accountNumber}, Số tiền: {amount}, Nội dung: {content}
            String qrContent = String.format(
                "STK: %s\nTên TK: %s\nSố tiền: %d VNĐ\nNội dung: %s",
                paymentData.getAccountNumber() != null ? paymentData.getAccountNumber() : "970422",
                paymentData.getAccountName() != null ? paymentData.getAccountName() : "NGUYEN VAN A",
                paymentData.getAmount() != null ? paymentData.getAmount() : 0,
                paymentData.getContent() != null ? paymentData.getContent() : "Thanh toán đơn hàng #" + paymentData.getOrderId()
            );
            
            // Generate QR code image (300x300 for better quality)
            byte[] qrImageBytes = qrCodeService.generateQRCodeImageBytes(qrContent, 300, 300);
            
            return ResponseEntity.ok()
                    .header("Content-Type", "image/png")
                    .body(qrImageBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage() != null ? e.getMessage() : "Failed to generate QR code"
            ));
        }
    }
}
