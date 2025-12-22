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
public class SepayPaymentResponse {
    private String transactionId;
    private Long orderId;
    private Long amount;
    private String accountNumber;
    private String accountName;
    private String bankCode;
    private String content;
    private String paymentUrl; // QR code image URL
    private String status;
    private LocalDateTime expiresAt;
}




