package com.restaurant.backend.Dto.Request;

import lombok.Data;

@Data
public class BookingItemRequest {
    private Long bookingId;
    private Long menuId;
    private int quantity;
    private double price;
}
