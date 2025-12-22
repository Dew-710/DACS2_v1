package com.restaurant.backend.Config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PayOSProperties {

    @Value("${payos.base-url}")
    private String baseUrl;

    @Value("${payos.client.id}")
    private String clientId;

    @Value("${payos.api.key}")
    private String apiKey;

    @Value("${payos.checksum.key}")
    private String checksumKey;

    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;

    @Value("${payos.webhook-header:X-Payos-Signature}")
    private String webhookHeader;

    @Value("${payos.timeout-ms:5000}")
    private int timeoutMs;

    @PostConstruct
    public void init() {
        // Validate critical credentials
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new RuntimeException("PayOS Client ID is missing! Please check your application.properties or environment variables (PAYOS_CLIENT_ID or payos.client.id)");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("PayOS API Key is missing! Please check your application.properties or environment variables (PAYOS_API_KEY or payos.api.key)");
        }
        if (checksumKey == null || checksumKey.trim().isEmpty()) {
            throw new RuntimeException("PayOS Checksum Key is missing! Please check your application.properties or environment variables (PAYOS_CHECKSUM_KEY or payos.checksum.key)");
        }
        
        // Validate checksum key format
        if (checksumKey != null && !checksumKey.trim().isEmpty()) {
            int keyLength = checksumKey.length();
            if (keyLength != 64) {
                throw new RuntimeException("PayOS Checksum Key length is " + keyLength + " (expected 64 characters). PayOS Checksum Key should be exactly 64 hexadecimal characters.");
            }
            boolean isValidHex = checksumKey.matches("^[0-9a-fA-F]{64}$");
            if (!isValidHex) {
                throw new RuntimeException("PayOS Checksum Key may contain invalid characters. PayOS Checksum Key should only contain hexadecimal characters (0-9, a-f, A-F).");
            }
        }
    }

    // Getters
    public String getBaseUrl() {
        return baseUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getChecksumKey() {
        return checksumKey;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public String getWebhookHeader() {
        return webhookHeader;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }
}
