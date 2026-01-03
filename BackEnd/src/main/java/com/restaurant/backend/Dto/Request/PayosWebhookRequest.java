package com.restaurant.backend.Dto.Request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayosWebhookRequest {
    private Long orderCode;
    private String code; // Payment status code from PayOS ("00" = success)
    private String reference; // Transaction reference from PayOS
    private BigDecimal amount; // Payment amount
    private String checksum; // Signature for verification
    private String desc; // Description
    private String paymentLinkId; // PayOS payment link ID
}

