package com.restaurant.backend.Controller;

import com.restaurant.backend.Entity.Order;
import com.restaurant.backend.Entity.OrderItem;
import com.restaurant.backend.Repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders/fix")
@RequiredArgsConstructor
@Slf4j
public class OrderFixController {

    private final OrderRepository orderRepository;

    /**
     * Fix all orders with totalAmount = 0 by confirming all items and recalculating
     */
    @PostMapping("/confirm-all-items")
    public ResponseEntity<?> fixOrdersWithZeroTotal() {
        try {
            // Find all orders with totalAmount = 0 or null
            List<Order> orders = orderRepository.findAll().stream()
                    .filter(order -> order.getTotalAmount() == null || 
                                   order.getTotalAmount().compareTo(BigDecimal.ZERO) == 0)
                    .filter(order -> !order.getOrderItems().isEmpty())
                    .toList();
            
            int fixedCount = 0;
            for (Order order : orders) {
                // Confirm all items
                for (OrderItem item : order.getOrderItems()) {
                    if (Boolean.FALSE.equals(item.getIsConfirmed())) {
                        item.setIsConfirmed(true);
                        item.setUpdatedAt(LocalDateTime.now());
                    }
                }
                
                // Recalculate total
                BigDecimal total = order.getOrderItems().stream()
                        .filter(item -> !"CANCELLED".equals(item.getStatus()))
                        .filter(item -> Boolean.TRUE.equals(item.getIsConfirmed()))
                        .map(item -> {
                            if (item.getSubtotal() != null) {
                                return item.getSubtotal();
                            } else if (item.getPrice() != null) {
                                return item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                            }
                            return BigDecimal.ZERO;
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                order.setTotalAmount(total);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                
                log.info("✅ Fixed order #{}: {} items confirmed, new total: {} VND", 
                        order.getId(), order.getOrderItems().size(), total);
                fixedCount++;
            }
            
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Fixed " + fixedCount + " orders",
                            "fixedOrders", fixedCount,
                            "details", "All unconfirmed items have been confirmed and totals recalculated"
                    )
            );
        } catch (Exception e) {
            log.error("Failed to fix orders: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "message", "Failed to fix orders: " + e.getMessage(),
                            "status", "error"
                    )
            );
        }
    }

    /**
     * Fix a specific order by ID
     */
    @PostMapping("/confirm-items/{orderId}")
    public ResponseEntity<?> fixSpecificOrder(@PathVariable Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // Confirm all items
            int confirmedCount = 0;
            for (OrderItem item : order.getOrderItems()) {
                if (Boolean.FALSE.equals(item.getIsConfirmed())) {
                    item.setIsConfirmed(true);
                    item.setUpdatedAt(LocalDateTime.now());
                    confirmedCount++;
                }
            }
            
            // Recalculate total
            BigDecimal total = order.getOrderItems().stream()
                    .filter(item -> !"CANCELLED".equals(item.getStatus()))
                    .filter(item -> Boolean.TRUE.equals(item.getIsConfirmed()))
                    .map(item -> {
                        if (item.getSubtotal() != null) {
                            return item.getSubtotal();
                        } else if (item.getPrice() != null) {
                            return item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                        }
                        return BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            order.setTotalAmount(total);
            order.setUpdatedAt(LocalDateTime.now());
            Order savedOrder = orderRepository.save(order);
            
            log.info("✅ Fixed order #{}: {} items confirmed, new total: {} VND", 
                    orderId, confirmedCount, total);
            
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Order fixed successfully",
                            "order", savedOrder,
                            "confirmedItems", confirmedCount,
                            "newTotal", total
                    )
            );
        } catch (Exception e) {
            log.error("Failed to fix order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "message", "Failed to fix order: " + e.getMessage(),
                            "status", "error"
                    )
            );
        }
    }
}


