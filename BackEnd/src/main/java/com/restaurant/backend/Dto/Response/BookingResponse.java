package com.restaurant.backend.Dto.Response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class BookingResponse {
    private Integer id;
    private UserResponse customer;
    private TableResponse table;
    private LocalDate date;
    private LocalTime time;
    private int guests;
    private String note;
    private String status;

    private List<BookingItemResponse> items;
}
