package com.restaurant.backend.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payos_payment_id", unique = true, length = 200)
    private String payosPaymentId;

    @Column(name = "internal_reference", unique = true, nullable = false, length = 200)
    private String internalReference;

    @Column(name = "payment_order_code")
    private Long paymentOrderCode;

    private BigDecimal amount;

    @Column(length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PaymentStatus status;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "reference", length = 200)
    private String reference;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;
}
