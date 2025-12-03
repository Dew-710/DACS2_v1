package com.restaurant.backend.Service.impl;

import com.restaurant.backend.Entity.Order;
import com.restaurant.backend.Entity.Payment;
import com.restaurant.backend.Repository.OrderRepository;
import com.restaurant.backend.Repository.PaymentRepository;
import com.restaurant.backend.Service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Override
    public Payment create(Payment payment) {
        payment.setPaidAt(LocalDateTime.now());
        if (payment.getStatus() == null) {
            payment.setStatus("PENDING");
        }
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getAll() {
        return paymentRepository.findAll();
    }

    @Override
    public Payment update(Long id, Payment payment) {
        Payment existingPayment = getById(id);
        existingPayment.setStatus(payment.getStatus());
        existingPayment.setNotes(payment.getNotes());
        return paymentRepository.save(existingPayment);
    }

    @Override
    public void delete(Long id) {
        paymentRepository.deleteById(id);
    }

    @Override
    public Payment processPayment(Long orderId, Payment payment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaidAt(LocalDateTime.now());
        payment.setStatus("COMPLETED");

        return paymentRepository.save(payment);
    }

    @Override
    public Payment refundPayment(Long paymentId, BigDecimal amount) {
        Payment payment = getById(paymentId);
        payment.setStatus("REFUNDED");
        payment.setNotes("Refunded amount: " + amount);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentByOrder(Long orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        return payments.isEmpty() ? null : payments.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByMethod(String method) {
        return paymentRepository.findByMethod(method);
    }
}
