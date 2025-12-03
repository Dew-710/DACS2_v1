package com.restaurant.backend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_name", nullable = false, length = 50)
    private String tableName;

    private int capacity;

    @Column(length = 20)
    private String status; // VACANT, RESERVED, OCCUPIED, CLEANING

    @Column(name = "qr_code", unique = true, length = 100)
    private String qrCode; // Unique QR code for each table

    @Column(name = "table_type", length = 20)
    private String tableType; // WINDOW, INDOOR, OUTDOOR, VIP, etc.

    @Column(name = "location", length = 50)
    private String location; // Floor, area description

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "table")
    @JsonIgnore
    private List<Booking> bookings;

    @OneToMany(mappedBy = "table")
    @JsonIgnore
    private List<Order> orders;
}
