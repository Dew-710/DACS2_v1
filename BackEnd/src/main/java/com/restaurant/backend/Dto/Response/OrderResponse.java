package com.restaurant.backend.Dto.Response;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
@Data
public class OrderResponse {
    private Integer id;
    private UserResponse customer;
    private UserResponse staff;
    private TableResponse table;
    private BookingResponse booking;

    private LocalDateTime orderTime;
    private String status;
    private double total;

    private List<OrderItemResponse> items;
}
