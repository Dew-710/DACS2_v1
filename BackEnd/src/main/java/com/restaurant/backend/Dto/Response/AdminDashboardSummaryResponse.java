package com.restaurant.backend.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardSummaryResponse {
    private BigDecimal totalRevenue;
    private Long activeOrders;
    private Long availableTables;
    private Long totalTables;
    private Long pendingReservations;
}








