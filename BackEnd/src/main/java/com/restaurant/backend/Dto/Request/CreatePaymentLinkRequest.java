package com.restaurant.backend.Dto.Request;

import com.restaurant.backend.Dto.PayOS.PaymentLinkItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentLinkRequest {
    // ✅ REQUIRED: PayOS yêu cầu orderCode kiểu Long (không phải String!)
    private Long orderCode;
    
    // ✅ REQUIRED: Số tiền thanh toán (phải > 0)
    private Long amount;
    
    // ✅ REQUIRED: Mô tả đơn hàng
    private String description;
    
    // ✅ REQUIRED: URL khi thanh toán thành công
    private String returnUrl;
    
    // ✅ REQUIRED: URL khi hủy thanh toán
    private String cancelUrl;
    
    // Optional fields
    private String currency;
    private List<PaymentLinkItem> items;
    private Map<String, String> metadata;
}

