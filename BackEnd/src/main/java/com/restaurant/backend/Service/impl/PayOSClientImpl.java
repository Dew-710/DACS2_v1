package com.restaurant.backend.Service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.backend.Config.PayOSProperties;
import com.restaurant.backend.Dto.Request.CreatePaymentLinkRequest;
import com.restaurant.backend.Dto.Response.CreatePaymentLinkResponse;
import com.restaurant.backend.Dto.PayOS.PayOSWebhook;
import com.restaurant.backend.Service.PayOSClient;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PayOSClientImpl implements PayOSClient {

    private final WebClient webClient;
    private final PayOSProperties properties;
    private final ObjectMapper mapper = new ObjectMapper();

    public PayOSClientImpl(WebClient payOSWebClient, PayOSProperties properties) {
        this.webClient = payOSWebClient;
        this.properties = properties;
    }

    @Override
    public CreatePaymentLinkResponse createPaymentLink(CreatePaymentLinkRequest request) throws Exception {
        // ✅ VALIDATION: Kiểm tra tất cả required fields theo PayOS API
        if (request.getOrderCode() == null) {
            throw new IllegalArgumentException("orderCode is required (must be Long/number)");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("description is required");
        }
        if (request.getReturnUrl() == null || request.getReturnUrl().isBlank()) {
            throw new IllegalArgumentException("returnUrl is required");
        }
        if (request.getCancelUrl() == null || request.getCancelUrl().isBlank()) {
            throw new IllegalArgumentException("cancelUrl is required");
        }
        
        // ✅ Log request để debug
        System.out.println("=== Sending to PayOS API ===");
        String requestJson = mapper.writeValueAsString(request);
        System.out.println("Request JSON: " + requestJson);
        System.out.println("===========================");
        
        // Call PayOS endpoint - POST /payment_links
        String resp = webClient.post()
                .uri("/payment_links")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(requestJson)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    clientResponse -> clientResponse.bodyToMono(String.class).map(errorBody -> {
                        System.err.println("❌ PayOS API Error Response: " + errorBody);
                        return new RuntimeException("PayOS API Error: " + errorBody);
                    })
                )
                .bodyToMono(String.class)
                .block();

        CreatePaymentLinkResponse out = new CreatePaymentLinkResponse();
        // fallback defaults
        out.setInternalReference(request.getMetadata() != null ? request.getMetadata().get("internalReference") : null);
        out.setAmount(request.getAmount());

        if (resp != null && !resp.isBlank()) {
            System.out.println("✅ PayOS API Response: " + resp);
            JsonNode node = mapper.readTree(resp);
            
            // Common fields: id, url (or payment_url), expires_at, status
            String id = node.has("id") ? node.get("id").asText(null)
                    : node.has("payos_payment_id") ? node.get("payos_payment_id").asText(null) : null;
            String url = node.has("url") ? node.get("url").asText(null)
                    : node.has("payment_url") ? node.get("payment_url").asText(null)
                    : node.has("checkoutUrl") ? node.get("checkoutUrl").asText(null) : null;
            String status = node.has("status") ? node.get("status").asText(null)
                    : node.has("state") ? node.get("state").asText(null) : null;
            String expiresAtRaw = node.has("expires_at") ? node.get("expires_at").asText(null)
                    : node.has("expiresAt") ? node.get("expiresAt").asText(null) : null;

            out.setPayosPaymentId(id);
            out.setPaymentUrl(url);
            out.setStatus(status != null ? status : "CREATED");
            if (expiresAtRaw != null) {
                try {
                    // try parse ISO date-time
                    LocalDateTime dt = LocalDateTime.parse(expiresAtRaw, DateTimeFormatter.ISO_DATE_TIME);
                    out.setExpiresAt(dt);
                } catch (Exception ex) {
                    // ignore parsing error and leave expiresAt null
                }
            }
        }

        return out;
    }

    @Override
    public PayOSWebhook verifyAndParseWebhook(String signatureHeader, String rawBody) throws Exception {
        // compute HMAC-SHA256(rawBody, checksumKey)
        String computed = hmacHex(properties.getChecksumKey(), rawBody);
        if (signatureHeader == null || !signatureHeader.equalsIgnoreCase(computed)) {
            throw new RuntimeException("Invalid webhook signature");
        }
        return mapper.readValue(rawBody, PayOSWebhook.class);
    }

    private String hmacHex(String key, String data) throws Exception {
        if (key == null) key = "";
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(digest);
    }
}
