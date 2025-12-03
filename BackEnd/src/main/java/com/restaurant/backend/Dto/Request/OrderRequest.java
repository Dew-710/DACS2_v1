package com.restaurant.backend.Dto.Request;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private Long customerId;
    private Long tableId;
    private Long staffId;
    private Long bookingId;
    private String status;
    private List<OrderItemRequest> items;
}
