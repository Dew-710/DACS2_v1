package com.restaurant.backend.Dto.Response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Integer id;
    private OrderResponse order;
    private Long amount;
    private String method;
    private LocalDateTime paidAt;
}
