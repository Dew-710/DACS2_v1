package com.restaurant.backend.Dto.Request;
import lombok.*;


@Getter
@Setter
public class OrderItemRequest {
    private Long menuId;
    private int quantity;
}
