package com.restaurant.backend.Dto.Request;
import lombok.Data;
@Data
public class PaymentRequest {
    private Integer orderId;
    private double amount;
    private String method; // CASH / CARD / BANK
}
