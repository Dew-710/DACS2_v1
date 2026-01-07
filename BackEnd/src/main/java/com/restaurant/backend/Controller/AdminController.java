package com.restaurant.backend.Controller;

import com.restaurant.backend.Dto.Response.AdminDashboardSummaryResponse;
import com.restaurant.backend.Dto.Response.RecentOrderResponse;
import com.restaurant.backend.Entity.Booking;
import com.restaurant.backend.Service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Management", description = "APIs for admin dashboard and management")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard/summary")
    @Operation(summary = "Get dashboard summary", description = "Get overview statistics for admin dashboard")
    public ResponseEntity<AdminDashboardSummaryResponse> getDashboardSummary() {
        log.info("Fetching dashboard summary");
        AdminDashboardSummaryResponse summary = adminService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/orders/recent")
    @Operation(summary = "Get recent orders", description = "Get latest orders sorted by creation date")
    public ResponseEntity<Map<String, Object>> getRecentOrders(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Fetching recent orders with limit: {}", limit);
        List<RecentOrderResponse> orders = adminService.getRecentOrders(limit);
        return ResponseEntity.ok(Map.of("orders", orders));
    }

    @GetMapping("/reservations/pending")
    @Operation(summary = "Get pending reservations", description = "Get all reservations with PENDING status")
    public ResponseEntity<Map<String, Object>> getPendingReservations() {
        log.info("Fetching pending reservations");
        List<Booking> reservations = adminService.getPendingReservations();
        return ResponseEntity.ok(Map.of("reservations", reservations));
    }

    @PatchMapping("/reservations/{id}/approve")
    @Operation(summary = "Approve reservation", description = "Approve a pending reservation")
    public ResponseEntity<Map<String, Object>> approveReservation(@PathVariable Long id) {
        log.info("Approving reservation with id: {}", id);
        Booking booking = adminService.approveReservation(id);
        return ResponseEntity.ok(Map.of(
                "message", "Reservation approved successfully",
                "reservation", booking
        ));
    }

    @PatchMapping("/reservations/{id}/reject")
    @Operation(summary = "Reject reservation", description = "Reject a pending reservation")
    public ResponseEntity<Map<String, Object>> rejectReservation(@PathVariable Long id) {
        log.info("Rejecting reservation with id: {}", id);
        Booking booking = adminService.rejectReservation(id);
        return ResponseEntity.ok(Map.of(
                "message", "Reservation rejected successfully",
                "reservation", booking
        ));
    }
}








