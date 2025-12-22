package com.restaurant.backend.Dto.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SepayPaymentRequest {
    private Long orderId;
    private Long amount;
    private String description;
}



