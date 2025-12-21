package com.restaurant.backend.Dto.PayOS;

import lombok.Data;

import java.util.Map;

@Data
public class PayOSWebhook {
    private String event;
    private String timestamp;
    private Map<String, Object> data;
}

