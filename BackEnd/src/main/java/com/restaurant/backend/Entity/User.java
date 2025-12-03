package com.restaurant.backend.Entity;



import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    private String phone;
    private String email;

    @Column(nullable = false)
    private String role;   // ADMIN / STAFF / CUSTOMER

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, SUSPENDED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // USER → BOOKING
    @OneToMany(mappedBy = "customer")
    private List<Booking> bookings;

    // USER → ORDERS (AS CUSTOMER)
    @OneToMany(mappedBy = "customer")
    private List<Order> customerOrders;

    // USER → ORDERS (AS STAFF)
    @OneToMany(mappedBy = "staff")
    private List<Order> handledOrders;
}
