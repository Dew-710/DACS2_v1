package com.restaurant.backend.Service;

import vn.payos.model.webhooks.Webhook;

public interface PayOSWebhookService {
    /**
     * Process PayOS webhook payment notification
     * @param webhook The PayOS webhook payload
     * @return Success message
     */
    String processPaymentWebhook(Webhook webhook);
}

