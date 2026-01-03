package com.restaurant.backend.Service.impl;

/**
 * PayOS Service - Updated to use PayOS SDK v1.0.3
 * - Uses PaymentData instead of CreatePaymentLinkRequest
 * - Uses CheckoutResponseData instead of CreatePaymentLinkResponse
 * - Removed items parameter (not supported in new SDK)
 * - Updated method signature to match PayOSService interface
 * - Fixed incompatible types in ternary operator (.amount() requires long)
 */

import com.restaurant.backend.Config.PayOSProperties;
import com.restaurant.backend.Entity.*;
import com.restaurant.backend.Repository.*;
import com.restaurant.backend.Service.JwtService;
import com.restaurant.backend.Service.PayOSClient;
import com.restaurant.backend.Service.PayOSService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PayOSServiceImpl implements PayOSService {
    private final PayOSClient payOSClient;
    private final PayOSProperties payOSProperties;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public PayOSServiceImpl(PayOSClient payOSClient,
                        PayOSProperties payOSProperties,
                        JwtService jwtService,
                        UserRepository userRepository,
                        OrderRepository orderRepository,
                        WalletRepository walletRepository,
                        WalletTransactionRepository walletTransactionRepository,
                        PaymentRepository paymentRepository,
                        PaymentTransactionRepository paymentTransactionRepository) {
        this.payOSClient = payOSClient;
        this.payOSProperties = payOSProperties;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.paymentRepository = paymentRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    @Transactional
    public ResponseEntity<?> createLink(Long orderId, String token) throws Exception {
        // Validate input
        if (orderId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "ID đơn hàng không được để trống"));
        }

        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token xác thực không được cung cấp"));
        }

        // Extract username from token and get user
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Load order from database
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        // Validate order ownership - STAFF và ADMIN có quyền thanh toán tất cả đơn hàng
        // CUSTOMER chỉ có quyền thanh toán đơn hàng của chính họ
        // Walk-in orders (không có customer) chỉ STAFF/ADMIN mới được thanh toán
        boolean isStaffOrAdmin = "STAFF".equalsIgnoreCase(user.getRole()) || "ADMIN".equalsIgnoreCase(user.getRole());
        
        if (!isStaffOrAdmin) {
            // Nếu không phải STAFF/ADMIN, kiểm tra xem có phải customer của order không
            if (order.getCustomer() == null) {
                // Walk-in order - chỉ STAFF/ADMIN mới được thanh toán
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Bạn không có quyền thanh toán đơn hàng này: #" + order.getId()));
            }
            
            // Kiểm tra xem user có phải là customer của order không
            if (!order.getCustomer().getId().equals(user.getId())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Bạn không có quyền thanh toán đơn hàng này: #" + order.getId()));
            }
        }
        // Nếu là STAFF hoặc ADMIN thì cho phép thanh toán tất cả đơn hàng

        // Validate order status - check if cancelled or already paid
        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Đơn hàng #" + order.getId() + " đã bị hủy và không thể thanh toán"));
        }

        if ("PAID".equalsIgnoreCase(order.getPaymentStatus())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Đơn hàng #" + order.getId() + " đã được thanh toán"));
        }

        // Get total amount from order
        BigDecimal totalAmount = order.getTotalAmount();
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Tổng tiền thanh toán phải lớn hơn 0"));
        }

        // Build items list from order items
        List<PaymentLinkItem> items = new ArrayList<>();
        if (order.getOrderItems() != null) {
            for (OrderItem orderItem : order.getOrderItems()) {
                if (orderItem.getMenuItem() != null && orderItem.getPrice() != null) {
                    items.add(
                            PaymentLinkItem.builder()
                                    .name(orderItem.getMenuItem().getName())
                                    .quantity(orderItem.getQuantity())
                                    .price(orderItem.getPrice().longValue())
                                    .build()
                    );
                }
            }
        }

        // Generate unique orderCode (timestamp-based)
        long paymentOrderCode = Long.parseLong(System.currentTimeMillis() + String.format("%03d", order.getId() % 1000));

        // Build description (max 25 characters for PayOS)
        String description = "Thanh toan " + order.getId() + " don hang";
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        // Initialize PayOS with properties
        PayOS payOS = new PayOS(
                payOSProperties.getClientId(),
                payOSProperties.getApiKey(),
                payOSProperties.getChecksumKey()
        );

        // Create payment request
        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(paymentOrderCode)
                .amount(totalAmount.longValue())
                .description(description)
                .items(items)
                .returnUrl(payOSProperties.getReturnUrl())
                .cancelUrl(payOSProperties.getCancelUrl())
                .build();

        try {
            // Create payment link via PayOS
            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

            // Create PaymentTransaction record
            String internalReference = UUID.randomUUID().toString();
            PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                    .internalReference(internalReference)
                    .paymentOrderCode(paymentOrderCode)
                    .amount(totalAmount)
                    .currency("VND")
                    .status(PaymentStatus.PENDING)
                    .paymentMethod("PAYOS")
                    .description(description)
                    .createdBy(user)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            PaymentTransaction savedTransaction = paymentTransactionRepository.save(paymentTransaction);

            // Create Payment record to link with Order
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(totalAmount)
                    .method("PAYOS")
                    .status("PENDING")
                    .transactionId(String.valueOf(paymentOrderCode))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);
            log.info("Created Payment record for orderId: {}, transactionId: {}", orderId, paymentOrderCode);

            // Build response matching frontend CreatePaymentLinkResponse format
            Map<String, Object> response = new HashMap<>();
            response.put("paymentUrl", data.getCheckoutUrl()); // Frontend expects paymentUrl
            response.put("checkoutUrl", data.getCheckoutUrl()); // Keep for backward compatibility
            response.put("qrCode", data.getQrCode());
            response.put("orderCode", data.getOrderCode());
            response.put("amount", totalAmount.longValue());
            response.put("internalReference", internalReference);
            // PayOS SDK response may have different field names, use orderCode as payment ID if needed
            response.put("payosPaymentId", String.valueOf(data.getOrderCode()));
            response.put("status", "CREATED");
            response.put("orderId", orderId);

            log.info("Payment link created successfully for order: {}, orderCode: {}", orderId, paymentOrderCode);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error creating PayOS payment link: ", e);
            throw new RuntimeException("Lỗi PayOS: " + e.getMessage());
        }
    }
}