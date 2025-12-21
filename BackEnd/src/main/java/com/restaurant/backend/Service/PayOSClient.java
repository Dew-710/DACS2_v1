package com.restaurant.backend.Service;

import com.restaurant.backend.Dto.Request.CreatePaymentLinkRequest;
import com.restaurant.backend.Dto.Response.CreatePaymentLinkResponse;
import com.restaurant.backend.Dto.PayOS.PayOSWebhook;

public interface PayOSClient {
    CreatePaymentLinkResponse createPaymentLink(CreatePaymentLinkRequest request) throws Exception;
    PayOSWebhook verifyAndParseWebhook(String signatureHeader, String rawBody) throws Exception;
}

