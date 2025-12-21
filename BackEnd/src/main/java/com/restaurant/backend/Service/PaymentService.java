package com.restaurant.backend.Service;

import com.restaurant.backend.Dto.Request.PayosWebhookRequest;
import com.restaurant.backend.Dto.Response.PaymentLinkResponse;
import com.restaurant.backend.Entity.Payment;

import java.util.List;

public interface PaymentService {

    // Basic CRUD operations
    Payment create(Payment payment);
    Payment getById(Long id);
    List<Payment> getAll();
    Payment update(Long id, Payment payment);
    void delete(Long id);

    // Business operations
    Payment processPayment(Long orderId, Payment payment);
    Payment refundPayment(Long paymentId, java.math.BigDecimal amount);

    // New methods
    PaymentLinkResponse createPaymentLink(String token, List<Long> orderIds);
    void handleWebhook(PayosWebhookRequest webhook) throws Exception;

    // Query operations
    List<Payment> getPaymentsByOrder(Long orderId);
    Payment getPaymentByOrder(Long orderId);
    List<Payment> getPaymentsByStatus(String status);
    List<Payment> getPaymentsByMethod(String method);
}
