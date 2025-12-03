package com.restaurant.backend.Dto.Request;
import lombok.Data;
@Data
public class MenuItemRequest {
    private String name;
    private double price;
    private String description;
    private String imageUrl;
    private Integer categoryId;
    private String status;
}
