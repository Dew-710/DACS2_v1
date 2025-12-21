package com.restaurant.backend.Dto.Request;

import lombok.Data;

@Data
public class PayosWebhookRequest {
    private Long orderCode;
    private String code;
    private String reference;
}

