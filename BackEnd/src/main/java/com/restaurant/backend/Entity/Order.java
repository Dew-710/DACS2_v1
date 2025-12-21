package com.restaurant.backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user = customer
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    // user = staff who takes the order
    @ManyToOne
    @JoinColumn(name = "staff_id")
    private User staff;

    @ManyToOne
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "order_time")
    private LocalDateTime orderTime;

    // Order status: PLACED, CONFIRMED, PREPARING, READY, SERVED, PAID, CANCELLED
    private String status;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "estimated_ready_time")
    private LocalDateTime estimatedReadyTime;

    @Column(name = "actual_ready_time")
    private LocalDateTime actualReadyTime;

    @Column(name = "served_time")
    private LocalDateTime servedTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<OrderItem> orderItems;
}
