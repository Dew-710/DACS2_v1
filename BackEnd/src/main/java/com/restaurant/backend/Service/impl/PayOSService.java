package com.restaurant.backend.Service.impl;

import com.restaurant.backend.Dto.Request.CreatePaymentLinkRequest;
import com.restaurant.backend.Dto.Response.CreatePaymentLinkResponse;
import com.restaurant.backend.Dto.PayOS.PayOSWebhook;
import com.restaurant.backend.Entity.*;
import com.restaurant.backend.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PayOSService {

    private final PayOSClientImpl payOSClient;
    private final OrderRepository orderRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PaymentRepository paymentRepository;

    public PayOSService(PayOSClientImpl payOSClient,
                        OrderRepository orderRepository,
                        WalletRepository walletRepository,
                        WalletTransactionRepository walletTransactionRepository,
                        PaymentRepository paymentRepository) {
        this.payOSClient = payOSClient;
        this.orderRepository = orderRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public CreatePaymentLinkResponse createLink(CreatePaymentLinkRequest req, User user, List<Order> orders) throws Exception {
        // ✅ VALIDATION 1: Kiểm tra amount > 0
        if (req.getAmount() == null || req.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        
        // ✅ VALIDATION 2: Kiểm tra required fields
        if (req.getReturnUrl() == null || req.getReturnUrl().isBlank()) {
            throw new IllegalArgumentException("returnUrl is required");
        }
        if (req.getCancelUrl() == null || req.getCancelUrl().isBlank()) {
            throw new IllegalArgumentException("cancelUrl is required");
        }
        if (req.getDescription() == null || req.getDescription().isBlank()) {
            throw new IllegalArgumentException("description is required");
        }
        
        // ✅ VALIDATION 3: Kiểm tra URLs không phải localhost
        if (req.getReturnUrl().contains("localhost") || req.getCancelUrl().contains("localhost")) {
            System.err.println("⚠️ WARNING: PayOS không hỗ trợ localhost URLs!");
            System.err.println("   returnUrl: " + req.getReturnUrl());
            System.err.println("   cancelUrl: " + req.getCancelUrl());
            System.err.println("   → Dùng ngrok hoặc deploy lên server thực!");
        }
        
        // ✅ GENERATE orderCode: Sử dụng timestamp để tạo số duy nhất
        // PayOS yêu cầu orderCode phải là số nguyên dương (Long)
        if (req.getOrderCode() == null) {
            Long orderCode = System.currentTimeMillis(); // Unique timestamp
            req.setOrderCode(orderCode);
            System.out.println("✅ Auto-generated orderCode: " + orderCode);
        }
        
        System.out.println("=== PayOS Payment Request ===");
        System.out.println("orderCode: " + req.getOrderCode());
        System.out.println("amount: " + req.getAmount());
        System.out.println("description: " + req.getDescription());
        System.out.println("returnUrl: " + req.getReturnUrl());
        System.out.println("cancelUrl: " + req.getCancelUrl());
        System.out.println("items count: " + (req.getItems() != null ? req.getItems().size() : 0));
        System.out.println("=============================");;
        
        // generate an internal reference and create Payment rows for each order with transactionId = internalReference
        String internalReference = UUID.randomUUID().toString();

        List<Payment> paymentsCreated = new ArrayList<>();
        for (Order order : orders) {
            Payment p = Payment.builder()
                    .order(order)
                    .amount(order.getTotalAmount())
                    .method("PAYOS")
                    .status("PENDING")
                    .transactionId(internalReference)
                    .createdAt(LocalDateTime.now())
                    .build();
            paymentsCreated.add(paymentRepository.save(p));
        }

        // attach internalReference to request metadata
        if (req.getMetadata() != null) {
            req.getMetadata().put("internalReference", internalReference);
        }

        CreatePaymentLinkResponse resp = payOSClient.createPaymentLink(req);

        // update payments to reference external payosPaymentId (if returned)
        if (resp != null && resp.getPayosPaymentId() != null) {
            for (Payment p : paymentsCreated) {
                p.setTransactionId(resp.getPayosPaymentId());
                p.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(p);
            }
        }

        return resp;
    }

    @Transactional
    public void handleWebhook(String signatureHeader, String rawBody) throws Exception {
        PayOSWebhook webhook = payOSClient.verifyAndParseWebhook(signatureHeader, rawBody);
        if (webhook == null || webhook.getData() == null) return;

        Object pid = webhook.getData().get("payosPaymentId");
        String payosPaymentId = pid != null ? pid.toString() : null;
        if (payosPaymentId == null) throw new RuntimeException("Missing payosPaymentId in webhook");

        // Find payments by payosPaymentId (or by internalReference if PayOS didn't return external id)
        List<Payment> payments = paymentRepository.findByTransactionId(payosPaymentId);

        Object statusObj = webhook.getData().get("status");
        String status = statusObj != null ? statusObj.toString() : null;

        if ("COMPLETED".equalsIgnoreCase(status) || "00".equals(status)) {
            if (payments != null && !payments.isEmpty()) {
                for (Payment p : payments) {
                    if (!"COMPLETED".equalsIgnoreCase(p.getStatus())) {
                        p.setStatus("COMPLETED");
                        p.setPaidAt(LocalDateTime.now());
                        p.setTransactionId(payosPaymentId);
                        p.setUpdatedAt(LocalDateTime.now());
                        paymentRepository.save(p);

                        Order order = p.getOrder();
                        if (order != null && (order.getStatus() == null || !"PAID".equalsIgnoreCase(order.getStatus()))) {
                            order.setStatus("PAID");
                            orderRepository.save(order);
                        }
                    }
                }
            } else {
                // no payment rows found for this transaction id -> attempt to credit wallet if webhook contains metadata identifying user
                Object metaUserId = webhook.getData().get("userId");
                if (metaUserId != null) {
                    Long userId = null;
                    try { userId = Long.parseLong(metaUserId.toString()); } catch (Exception ignored) {}
                    if (userId != null) {
                        Wallet wallet = walletRepository.findByUser_Id(userId).orElseThrow(() -> new RuntimeException("Wallet not found for user"));
                        // amount from webhook
                        Object amtObj = webhook.getData().get("amount");
                        BigDecimal amount = BigDecimal.ZERO;
                        if (amtObj != null) {
                            try { amount = new BigDecimal(amtObj.toString()); } catch (Exception ignored) {}
                        }
                        BigDecimal before = wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO;
                        BigDecimal after = before.add(amount);
                        wallet.setBalance(after);
                        wallet.setUpdatedAt(LocalDateTime.now());
                        walletRepository.save(wallet);

                        WalletTransaction walletTx = WalletTransaction.builder()
                                .wallet(wallet)
                                .amount(amount)
                                .beforeBalance(before)
                                .afterBalance(after)
                                .type("TOP_UP")
                                .description("Top up via PayOS webhook")
                                .createdAt(LocalDateTime.now())
                                .build();
                        walletTransactionRepository.save(walletTx);
                    }
                }
            }
        } else {
            // failed or cancelled
            if (payments != null && !payments.isEmpty()) {
                for (Payment p : payments) {
                    p.setStatus("FAILED");
                    p.setUpdatedAt(LocalDateTime.now());
                    paymentRepository.save(p);
                }
            }
        }
    }
}

