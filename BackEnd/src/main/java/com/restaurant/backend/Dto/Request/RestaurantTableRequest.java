package com.restaurant.backend.Dto.Request;
import lombok.Data;
@Data
public class RestaurantTableRequest {
    private String tableName;
    private int capacity;
    private String status;   // AVAILABLE / RESERVED
}
