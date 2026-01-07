package com.restaurant.backend.Service.impl;

import com.restaurant.backend.Dto.Response.AdminDashboardSummaryResponse;
import com.restaurant.backend.Dto.Response.RecentOrderResponse;
import com.restaurant.backend.Entity.Booking;
import com.restaurant.backend.Entity.Order;
import com.restaurant.backend.Entity.Payment;
import com.restaurant.backend.Entity.RestaurantTable;
import com.restaurant.backend.Repository.BookingRepository;
import com.restaurant.backend.Repository.OrderRepository;
import com.restaurant.backend.Repository.PaymentRepository;
import com.restaurant.backend.Repository.RestaurantTableRepository;
import com.restaurant.backend.Service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RestaurantTableRepository tableRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getDashboardSummary() {
        // Calculate total revenue from COMPLETED payments only
        List<Payment> paidPayments = paymentRepository.findByStatus("COMPLETED");
        BigDecimal totalRevenue = paidPayments.stream()
                .map(Payment::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Count active orders (status != COMPLETED, != CANCELLED)
        long activeOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() != null &&
                        !order.getStatus().equals("COMPLETED") &&
                        !order.getStatus().equals("CANCELLED"))
                .count();

        // Count available tables (total - occupied)
        List<RestaurantTable> allTables = tableRepository.findAll();
        long totalTables = allTables.size();
        long occupiedTables = allTables.stream()
                .filter(table -> table.getStatus() != null &&
                        table.getStatus().equals("OCCUPIED"))
                .count();
        long availableTables = totalTables - occupiedTables;

        // Count pending reservations
        List<Booking> pendingBookings = bookingRepository.findByStatus("PENDING");
        long pendingReservations = pendingBookings.size();

        return AdminDashboardSummaryResponse.builder()
                .totalRevenue(totalRevenue)
                .activeOrders(activeOrders)
                .availableTables(availableTables)
                .totalTables(totalTables)
                .pendingReservations(pendingReservations)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecentOrderResponse> getRecentOrders(int limit) {
        List<Order> allOrders = orderRepository.findAll();
        
        // Sort by createdAt DESC and limit
        List<Order> orders = allOrders.stream()
                .sorted((o1, o2) -> {
                    if (o1.getCreatedAt() == null && o2.getCreatedAt() == null) return 0;
                    if (o1.getCreatedAt() == null) return 1;
                    if (o2.getCreatedAt() == null) return -1;
                    return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                })
                .limit(limit)
                .collect(Collectors.toList());

        return orders.stream()
                .map(order -> RecentOrderResponse.builder()
                        .orderId(order.getId())
                        .createdAt(order.getCreatedAt())
                        .totalAmount(order.getTotalAmount())
                        .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus() : "WAITING_PAYMENT")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getPendingReservations() {
        return bookingRepository.findByStatus("PENDING");
    }

    @Override
    public Booking approveReservation(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new RuntimeException("Reservation is not in PENDING status");
        }

        booking.setStatus("CONFIRMED");
        booking.setUpdatedAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    @Override
    public Booking rejectReservation(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new RuntimeException("Reservation is not in PENDING status");
        }

        booking.setStatus("CANCELLED");
        booking.setUpdatedAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }
}

