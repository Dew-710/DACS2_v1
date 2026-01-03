package com.restaurant.backend.Service;

import com.restaurant.backend.Dto.Request.PayosWebhookRequest;

public interface PayOSWebhookHandlerService {
    /**
     * Process PayOS webhook payment notification
     * @param webhookRequest The webhook payload from PayOS
     * @return Success message
     */
    String processPaymentWebhook(PayosWebhookRequest webhookRequest);
}
