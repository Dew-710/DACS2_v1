package com.restaurant.backend.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SepayPaymentStatusResponse {
    private String transactionId;
    private Long orderId;
    private Long amount;
    private String status; // PENDING, COMPLETED, FAILED, CANCELLED
    private LocalDateTime paidAt;
    private String bankTransactionId;
}



