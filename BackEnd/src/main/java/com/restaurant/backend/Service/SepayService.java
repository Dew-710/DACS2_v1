package com.restaurant.backend.Service;

import com.restaurant.backend.Dto.Request.SepayPaymentRequest;
import com.restaurant.backend.Dto.Response.SepayPaymentResponse;
import com.restaurant.backend.Dto.Response.SepayPaymentStatusResponse;

public interface SepayService {
    SepayPaymentResponse createPayment(SepayPaymentRequest request);
    SepayPaymentStatusResponse getPaymentStatus(String transactionId);
    SepayPaymentResponse getPaymentData(String transactionId);
    boolean cancelPayment(String transactionId);
}



