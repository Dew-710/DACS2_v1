package com.restaurant.backend.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentLinkResponse {
    private String payosPaymentId;
    private String paymentUrl;
    private LocalDateTime expiresAt;
    private String status;
    private String internalReference;
    private Long amount; // Changed from BigDecimal to Long to match PayOS API
}

