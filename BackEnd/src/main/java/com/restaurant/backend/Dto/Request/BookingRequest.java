package com.restaurant.backend.Dto.Request;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.*;
import java.util.List;

@Getter
@Setter
public class BookingRequest {
    private Long tableId;
    private LocalDate date;
    private LocalTime time;
    private int guests;
    private String note;
    private List<BookingItemRequest> items;  // ⭐ Thêm dòng này
}
