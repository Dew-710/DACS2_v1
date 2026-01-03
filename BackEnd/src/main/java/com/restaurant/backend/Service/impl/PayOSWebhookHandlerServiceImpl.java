package com.restaurant.backend.Service.impl;

import com.restaurant.backend.Dto.Request.PayosWebhookRequest;
import com.restaurant.backend.Entity.Order;
import com.restaurant.backend.Entity.Payment;
import com.restaurant.backend.Entity.PaymentStatus;
import com.restaurant.backend.Entity.PaymentTransaction;
import com.restaurant.backend.Repository.OrderRepository;
import com.restaurant.backend.Repository.PaymentRepository;
import com.restaurant.backend.Repository.PaymentTransactionRepository;
import com.restaurant.backend.Service.PayOSWebhookHandlerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayOSWebhookHandlerServiceImpl implements PayOSWebhookHandlerService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public String processPaymentWebhook(PayosWebhookRequest webhookRequest) {
        log.info("Processing PayOS webhook - OrderCode: {}, Code: {}, Reference: {}",
                webhookRequest.getOrderCode(), webhookRequest.getCode(), webhookRequest.getReference());

        // Validate webhook request
        if (webhookRequest.getOrderCode() == null) {
            log.error("Invalid webhook request: orderCode is null");
            return "Invalid request: orderCode is required";
        }

        try {
            // Find the payment transaction by orderCode
            Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository
                    .findByPaymentOrderCode(webhookRequest.getOrderCode());

            if (transactionOpt.isEmpty()) {
                log.warn("Payment transaction not found for orderCode: {}", webhookRequest.getOrderCode());
                return "Payment transaction not found";
            }

            PaymentTransaction transaction = transactionOpt.get();
            log.info("Found payment transaction: ID={}, Status={}, OrderCode={}",
                    transaction.getId(), transaction.getStatus(), transaction.getPaymentOrderCode());

            // Check for idempotency - if already processed, return success
            if (transaction.getStatus() == PaymentStatus.PAID) {
                log.info("Payment already processed for orderCode: {} - Idempotent response", webhookRequest.getOrderCode());
                return "Payment already processed";
            }

            // Process based on payment status
            if ("00".equals(webhookRequest.getCode())) {
                // Payment successful
                return handlePaymentSuccess(transaction, webhookRequest);
            } else {
                // Payment failed or cancelled
                return handlePaymentFailure(transaction, webhookRequest);
            }

        } catch (Exception e) {
            log.error("Error processing PayOS webhook for orderCode: {}", webhookRequest.getOrderCode(), e);
            throw new RuntimeException("Failed to process webhook: " + e.getMessage());
        }
    }

    private String handlePaymentSuccess(PaymentTransaction transaction, PayosWebhookRequest webhookRequest) {
        log.info("Processing successful payment for orderCode: {}", webhookRequest.getOrderCode());

        try {
            // Update PaymentTransaction
            transaction.setStatus(PaymentStatus.PAID);
            transaction.setPaidAt(LocalDateTime.now());
            transaction.setReference(webhookRequest.getReference());
            transaction.setUpdatedAt(LocalDateTime.now());
            paymentTransactionRepository.save(transaction);
            log.info("Updated PaymentTransaction status to PAID for orderCode: {}", webhookRequest.getOrderCode());

            // Find related Order and Payment entities
            // We need to find orders that are linked to this transaction
            // Since PaymentTransaction doesn't directly link to Order, we might need to find Payment records
            // or use the orderId that was stored during payment creation

            // For now, let's find Payment records by transactionId or orderCode
            // Since the requirements mention updating Order.status and Payment.status,
            // we need to find the related entities

            // Try to find Payment by transactionId (which is the paymentOrderCode)
            List<Payment> payments = paymentRepository.findByTransactionId(String.valueOf(webhookRequest.getOrderCode()));

            if (!payments.isEmpty()) {
                Payment payment = payments.get(0); // Get the first payment if multiple exist
                Order order = payment.getOrder();

                // Update Order payment status
                order.setPaymentStatus("PAID");
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                log.info("Updated Order paymentStatus to PAID for orderId: {}", order.getId());

                // Update Payment status
                payment.setStatus("COMPLETED");
                payment.setTransactionId(webhookRequest.getReference());
                payment.setPaidAt(LocalDateTime.now());
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                log.info("Updated Payment status to COMPLETED for paymentId: {}", payment.getId());

            } else {
                log.warn("No Payment record found for orderCode: {} - only PaymentTransaction was updated. Payment record should be created when creating payment link.", webhookRequest.getOrderCode());
            }

            log.info("Successfully processed payment success for orderCode: {}", webhookRequest.getOrderCode());
            return "Payment processed successfully";

        } catch (Exception e) {
            log.error("Error handling payment success for orderCode: {}", webhookRequest.getOrderCode(), e);
            throw new RuntimeException("Failed to process payment success: " + e.getMessage());
        }
    }

    private String handlePaymentFailure(PaymentTransaction transaction, PayosWebhookRequest webhookRequest) {
        log.info("Processing failed payment for orderCode: {}", webhookRequest.getOrderCode());

        try {
            // Update PaymentTransaction status to cancelled
            transaction.setStatus(PaymentStatus.CANCELLED);
            transaction.setReference(webhookRequest.getReference());
            transaction.setUpdatedAt(LocalDateTime.now());
            paymentTransactionRepository.save(transaction);

            log.info("Updated PaymentTransaction status to CANCELLED for orderCode: {}", webhookRequest.getOrderCode());
            return "Payment failure processed";

        } catch (Exception e) {
            log.error("Error handling payment failure for orderCode: {}", webhookRequest.getOrderCode(), e);
            throw new RuntimeException("Failed to process payment failure: " + e.getMessage());
        }
    }
}
