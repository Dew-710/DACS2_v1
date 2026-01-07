package com.restaurant.backend.Service;

import com.restaurant.backend.Dto.Response.AdminDashboardSummaryResponse;
import com.restaurant.backend.Dto.Response.RecentOrderResponse;
import com.restaurant.backend.Entity.Booking;
import com.restaurant.backend.Entity.Order;

import java.util.List;

public interface AdminService {
    AdminDashboardSummaryResponse getDashboardSummary();
    List<RecentOrderResponse> getRecentOrders(int limit);
    List<Booking> getPendingReservations();
    Booking approveReservation(Long id);
    Booking rejectReservation(Long id);
}








