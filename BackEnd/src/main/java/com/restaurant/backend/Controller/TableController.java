package com.restaurant.backend.Controller;

import com.restaurant.backend.Entity.Order;
import com.restaurant.backend.Entity.RestaurantTable;
import com.restaurant.backend.Entity.User;
import com.restaurant.backend.Service.OrderService;
import com.restaurant.backend.Service.RestaurantTableService;
import com.restaurant.backend.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    private final RestaurantTableService tableService;
    private final OrderService orderService;
    private final UserService userService;

    public TableController(RestaurantTableService tableService,
                          OrderService orderService,
                          UserService userService) {
        this.tableService = tableService;
        this.orderService = orderService;
        this.userService = userService;
    }

    // Get all tables
    @GetMapping("/all")
    public ResponseEntity<?> getAllTables() {
        List<RestaurantTable> tables = tableService.getAll();
        return ResponseEntity.ok(
                Map.of(
                        "message", "Tables retrieved successfully",
                        "tables", tables
                )
        );
    }

    // Get available tables
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableTables() {
        List<RestaurantTable> tables = tableService.getAvailableTables();
        return ResponseEntity.ok(
                Map.of(
                        "message", "Available tables retrieved successfully",
                        "tables", tables
                )
        );
    }

    // Generate QR code for table
    @PostMapping("/{id}/generate-qr")
    public ResponseEntity<?> generateQrCode(@PathVariable Long id) {
        RestaurantTable table = tableService.generateQrCode(id);
        return ResponseEntity.ok(
                Map.of(
                        "message", "QR code generated successfully",
                        "table", table,
                        "qrCode", table.getQrCode()
                )
        );
    }

    // Check-in via QR code (customer scans QR at table)
    @PostMapping("/checkin/{qrCode}")
    public ResponseEntity<?> checkIn(@PathVariable String qrCode,
                                   @RequestParam Long customerId) {
        RestaurantTable table = tableService.checkInTable(qrCode, customerId);

        // Create initial order for the table
        Order order = new Order();
        User customer = userService.findById(customerId);
        order.setCustomer(customer);
        order.setTable(table);
        order.setOrderTime(LocalDateTime.now());
        order.setStatus("PLACED");

        Order createdOrder = orderService.create(order);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Check-in successful",
                        "table", table,
                        "order", createdOrder
                )
        );
    }

    // Get table by QR code
    @GetMapping("/qr/{qrCode}")
    public ResponseEntity<?> getTableByQr(@PathVariable String qrCode) {
        RestaurantTable table = tableService.findByQrCode(qrCode);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Table found",
                        "table", table
                )
        );
    }

    // Update table status (advanced)
    @PutMapping("/{id}/status-update/{status}")
    public ResponseEntity<?> updateTableStatus(@PathVariable Long id, @PathVariable String status) {
        RestaurantTable table = tableService.updateStatus(id, status);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Table status updated successfully",
                        "table", table
                )
        );
    }

    // Check-out table (after payment)
    @PostMapping("/{id}/checkout")
    public ResponseEntity<?> checkOutTable(@PathVariable Long id) {
        RestaurantTable table = tableService.checkOutTable(id);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Table checked out successfully",
                        "table", table
                )
        );
    }

    // Get table with current order
    @GetMapping("/{id}/current-order")
    public ResponseEntity<?> getCurrentOrder(@PathVariable Long id) {
        List<Order> activeOrders = orderService.getActiveOrdersByTable(id);

        if (activeOrders.isEmpty()) {
            return ResponseEntity.ok(
                    Map.of(
                            "message", "No active order for this table",
                            "hasActiveOrder", false
                    )
            );
        }

        Order currentOrder = activeOrders.get(0); // Get the first active order
        return ResponseEntity.ok(
                Map.of(
                        "message", "Current order retrieved",
                        "hasActiveOrder", true,
                        "order", currentOrder
                )
        );
    }
}
