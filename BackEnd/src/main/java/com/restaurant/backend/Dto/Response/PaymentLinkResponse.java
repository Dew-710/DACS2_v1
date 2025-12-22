package com.restaurant.backend.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLinkResponse {
    private String checkoutUrl;
    private String qrCode;
    private Long orderCode;
    private Long amount;
}

