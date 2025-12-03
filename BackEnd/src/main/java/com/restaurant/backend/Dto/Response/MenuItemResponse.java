package com.restaurant.backend.Dto.Response;

import lombok.Data;
@Data
public class MenuItemResponse {
    private Integer id;
    private String name;
    private double price;
    private String description;
    private String imageUrl;
    private String status;

    private CategoryResponse category;
}
