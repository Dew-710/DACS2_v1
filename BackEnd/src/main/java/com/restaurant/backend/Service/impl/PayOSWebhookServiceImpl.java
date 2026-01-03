package com.restaurant.backend.Service.impl;

import com.restaurant.backend.Config.PayOSProperties;
import com.restaurant.backend.Entity.Order;
import com.restaurant.backend.Entity.Payment;
import com.restaurant.backend.Repository.OrderRepository;
import com.restaurant.backend.Repository.PaymentRepository;
import com.restaurant.backend.Service.PayOSWebhookService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayOSWebhookServiceImpl implements PayOSWebhookService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PayOSProperties payOSProperties;

    @Override
    @Transactional
    public String processPaymentWebhook(Webhook webhook) {
        log.info("Received PayOS webhook");

        try {
            // Initialize PayOS SDK and verify webhook signature
            PayOS payOS = new PayOS(
                    payOSProperties.getClientId(),
                    payOSProperties.getApiKey(),
                    payOSProperties.getChecksumKey()
            );

            // Verify webhook signature using PayOS SDK
            WebhookData webhookData = payOS.webhooks().verify(webhook);
            
            Long orderCode = webhookData.getOrderCode();
            String code = webhookData.getCode();
            String reference = webhookData.getReference();

            log.info("Webhook verified successfully - OrderCode: {}, Code: {}, Reference: {}", orderCode, code, reference);

            // Check if payment is successful (code "00" means success in PayOS)
            if (!"00".equals(code)) {
                log.info("Payment not successful - Code: {}, OrderCode: {}", code, orderCode);
                return "Payment not successful";
            }

            // Find Payment by transaction_id (which equals orderCode from PayOS)
            List<Payment> payments = paymentRepository.findByTransactionId(String.valueOf(orderCode));

            if (payments.isEmpty()) {
                log.warn("Payment not found for transactionId: {} (orderCode: {})", orderCode, orderCode);
                return "Payment not found";
            }

            Payment payment = payments.get(0);
            Order order = payment.getOrder();

            log.info("Found Payment: ID={}, Status={}, OrderID={}", payment.getId(), payment.getStatus(), order.getId());

            // Idempotent check: if payment status is already COMPLETED, do nothing
            if ("COMPLETED".equals(payment.getStatus())) {
                log.info("Payment already processed (status=COMPLETED) for transactionId: {} - Idempotent response", orderCode);
                return "Payment already processed";
            }

            // Update Payment
            payment.setStatus("COMPLETED");
            payment.setTransactionId(reference);
            payment.setPaidAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            log.info("Updated Payment status to COMPLETED for paymentId: {}, transactionId: {}", payment.getId(), orderCode);

            // Update Order
            order.setPaymentStatus("PAID");
            order.setStatus("CONFIRMED");
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            log.info("Updated Order paymentStatus to PAID and status to CONFIRMED for orderId: {}", order.getId());

            log.info("Successfully processed payment success for transactionId: {}", orderCode);
            return "Payment processed successfully";

        } catch (Exception e) {
            log.error("Error processing PayOS webhook", e);
            // Don't throw exception to prevent webhook retries - return error message instead
            return "Error processing webhook: " + e.getMessage();
        }
    }
}

