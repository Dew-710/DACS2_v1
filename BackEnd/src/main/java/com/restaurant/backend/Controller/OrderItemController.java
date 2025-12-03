package com.restaurant.backend.Controller;

import com.restaurant.backend.Entity.OrderItem;
import com.restaurant.backend.Service.OrderItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    private final OrderItemService orderItemService;

    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    // Create new order item
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody OrderItem item) {
        OrderItem created = orderItemService.create(item);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Order item created successfully",
                        "orderItem", created
                )
        );
    }

    // Get all order items
    @GetMapping("/list")
    public ResponseEntity<?> getAll() {
        List<OrderItem> items = orderItemService.getAll();
        return ResponseEntity.ok(
                Map.of(
                        "message", "Order items retrieved successfully",
                        "orderItems", items
                )
        );
    }

    // Get order item by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        OrderItem item = orderItemService.getById(id);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Order item retrieved successfully",
                        "orderItem", item
                )
        );
    }

    // Get items by order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getByOrder(@PathVariable Long orderId) {
        List<OrderItem> items = orderItemService.getItemsByOrder(orderId);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Order items retrieved successfully",
                        "orderItems", items
                )
        );
    }

    // Get active items by order ID (not cancelled)
    @GetMapping("/order/{orderId}/active")
    public ResponseEntity<?> getActiveByOrder(@PathVariable Long orderId) {
        List<OrderItem> items = orderItemService.getActiveItemsByOrder(orderId);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Active order items retrieved successfully",
                        "orderItems", items
                )
        );
    }

    // Get items by status (for kitchen staff)
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(@PathVariable String status) {
        List<OrderItem> items = orderItemService.getItemsByStatus(status);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Order items retrieved successfully",
                        "orderItems", items
                )
        );
    }

    // Update order item
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody OrderItem item) {
        OrderItem updated = orderItemService.update(id, item);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Order item updated successfully",
                        "orderItem", updated
                )
        );
    }

    // Update order item status
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @PathVariable String status) {
        OrderItem updated = orderItemService.updateStatus(id, status);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Order item status updated successfully",
                        "orderItem", updated
                )
        );
    }

    // Delete order item
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        orderItemService.delete(id);
        return ResponseEntity.ok(
                Map.of("message", "Order item deleted successfully")
        );
    }
}
