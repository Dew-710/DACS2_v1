package com.restaurant.backend.Dto.Response;

import lombok.Data;
@Data
public class OrderItemResponse {
    private Integer id;
    private MenuItemResponse menuItem;
    private int quantity;
    private double price;
}
