package com.restaurant.backend.Controller;

import com.restaurant.backend.Entity.RestaurantTable;
import com.restaurant.backend.Service.RestaurantTableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tables")
public class RestaurantTableController {

    private final RestaurantTableService service;

    public RestaurantTableController(RestaurantTableService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAll() {
        List<RestaurantTable> tables = service.getAll();
        return ResponseEntity.ok(
                Map.of(
                        "message", "Tables retrieved successfully",
                        "tables", tables
                )
        );
    }

    @PostMapping("/fix-status")
    public ResponseEntity<?> fixTableStatuses() {
        try {
            List<RestaurantTable> tables = service.getAll();

            // Prioritize AVAILABLE status for booking
            // Set first 6 tables to AVAILABLE (available for booking)
            for (int i = 0; i < Math.min(6, tables.size()); i++) {
                RestaurantTable table = tables.get(i);
                table.setStatus("AVAILABLE");
                service.update(table.getId(), table);
            }

            // Set next 2 tables to OCCUPIED (currently in use)
            for (int i = 6; i < Math.min(8, tables.size()); i++) {
                RestaurantTable table = tables.get(i);
                table.setStatus("OCCUPIED");
                service.update(table.getId(), table);
            }

            // Set next 1 table to RESERVED (booked)
            for (int i = 8; i < Math.min(9, tables.size()); i++) {  
                RestaurantTable table = tables.get(i);
                table.setStatus("RESERVED");
                service.update(table.getId(), table);
            }

            // Keep remaining tables as MAINTENANCE or whatever they were

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Table statuses fixed successfully",
                            "updatedTables", tables.size(),
                            "vacantTables", Math.min(6, tables.size())
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("message", "Failed to fix table statuses: " + e.getMessage())
            );
        }
    }

    @PostMapping("/reset-all")
    public ResponseEntity<?> resetAllTables() {
        try {
            // Instead of deleting, just reset all statuses to AVAILABLE
            // This avoids foreign key constraint issues
            List<RestaurantTable> tables = service.getAll();

            for (RestaurantTable table : tables) {
                table.setStatus("AVAILABLE");
                service.update(table.getId(), table);
            }

            return ResponseEntity.ok(
                    Map.of(
                            "message", "All tables reset to AVAILABLE successfully",
                            "updatedTables", tables.size(),
                            "allVacant", true
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("message", "Failed to reset tables: " + e.getMessage())
            );
        }
    }

    @PostMapping("/force-vacant-all")
    public ResponseEntity<?> forceAllTablesVacant() {
        try {
            List<RestaurantTable> tables = service.getAll();

            // Force all tables to AVAILABLE status for immediate booking
            for (RestaurantTable table : tables) {
                table.setStatus("AVAILABLE");
                service.update(table.getId(), table);
            }

            return ResponseEntity.ok(
                    Map.of(
                            "message", "All tables set to AVAILABLE successfully",
                            "updatedTables", tables.size(),
                            "allVacant", true
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("message", "Failed to force VACANT status: " + e.getMessage())
            );
        }
    }

    private RestaurantTable createTable(String name, int capacity, String status, String type, String location) {
        RestaurantTable table = new RestaurantTable();
        table.setTableName(name);
        table.setCapacity(capacity);
        table.setStatus(status);
        table.setTableType(type);
        table.setLocation(location);
        table.setQrCode("TABLE-" + String.format("%03d", service.getAll().size() + 1));
        return table;
    }

    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @PathVariable String status) {
        RestaurantTable table = service.updateStatus(id, status);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Table status updated successfully",
                        "table", table
                )
        );
    }
}
