package com.restaurant.backend.Dto.Response;

import lombok.Data;

@Data
public class TableResponse {
    private Integer id;
    private String tableName;
    private int capacity;
    private String status;
}
