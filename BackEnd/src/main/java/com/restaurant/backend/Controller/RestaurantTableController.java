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
