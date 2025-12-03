package com.restaurant.backend.Dto.Response;

import lombok.Data;
@Data
public class BookingItemResponse {
    private Integer id;
    private MenuItemResponse menuItem;
    private int quantity;
    private double price;
}
