package com.restaurant.backend.Dto.PayOS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLinkItem {
    private String name;        // Tên sản phẩm
    private Integer quantity;   // Số lượng
    private Long price;         // Giá đơn vị (VND) - PayOS yêu cầu Long không phải BigDecimal
}

