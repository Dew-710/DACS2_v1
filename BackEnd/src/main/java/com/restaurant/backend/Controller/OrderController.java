package com.restaurant.backend.Controller;

import com.restaurant.backend.Dto.Request.OrderRequest;
import com.restaurant.backend.Entity.Booking;
import com.restaurant.backend.Entity.Order;
import com.restaurant.backend.Entity.OrderItem;
import com.restaurant.backend.Entity.User;
import com.restaurant.backend.Entity.RestaurantTable;
import com.restaurant.backend.Service.BookingService;
import com.restaurant.backend.Service.OrderService;
import com.restaurant.backend.Service.UserService;
import com.restaurant.backend.Service.RestaurantTableService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final RestaurantTableService tableService;
    private final BookingService bookingService;

    public OrderController(OrderService orderService, UserService userService, RestaurantTableService tableService, BookingService bookingService) {
        this.orderService = orderService;
        this.userService = userService;
        this.tableService = tableService;
        this.bookingService = bookingService;
    }

    // Create new order for a table
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Order order) {
        Order created = orderService.create(order);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Order created successfully",
                        "order", created
                )
        );
    }

    // Create new order with OrderRequest (supports customerId = null for walk-in)
    @PostMapping("/create-from-request")
    public ResponseEntity<?> createFromRequest(@RequestBody OrderRequest request) {
        System.out.println("📥 Received create-from-request");
        System.out.println("   Request: " + request);
        System.out.println("   TableId: " + request.getTableId());
        System.out.println("   CustomerId: " + request.getCustomerId());
        System.out.println("   BookingId: " + request.getBookingId());
        System.out.println("   Status: " + request.getStatus());
        System.out.println("   Items: " + (request.getItems() != null ? request.getItems().size() : "null"));
        
        // Validate table
        if (request.getTableId() == null) {
            System.err.println("❌ Table ID is null");
            return ResponseEntity.badRequest().body(Map.of("message", "Table ID is required"));
        }

        RestaurantTable table = tableService.findById(request.getTableId());
        if (table == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Table not found"));
        }

        // Booking: nếu có bookingId, LUÔN LUÔN dùng customer từ booking
        Booking booking = null;
        User customer = null;
        
        if (request.getBookingId() != null) {
            booking = bookingService.getBookingById(request.getBookingId());
            if (booking == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Booking not found"));
            }
            // ƯU TIÊN: Dùng customer từ booking (đảm bảo đúng customer đã đặt bàn)
            customer = booking.getCustomer();
            if (customer == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Booking does not have a customer"));
            }
        } else {
            // Chỉ dùng customer từ request nếu KHÔNG có booking
            if (request.getCustomerId() != null) {
                customer = userService.findById(request.getCustomerId());
                if (customer == null) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Customer not found"));
                }
            }
            // customer có thể null nếu walk-in (không có booking, không có customerId)
        }

        // Staff: nếu có
        User staff = null;
        if (request.getStaffId() != null) {
            staff = userService.findById(request.getStaffId());
            if (staff == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Staff not found"));
            }
        }

        // Build Order entity
        Order order = Order.builder()
                .customer(customer)  // Có thể null nếu walk-in
                .table(table)
                .staff(staff)
                .booking(booking)
                .status(request.getStatus() != null ? request.getStatus() : "PLACED")
                .build();

        Order created = orderService.create(order);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Order created successfully",
                        "order", created
                )
        );
    }

    // Create new order with customer ID and table ID (for check-in)
    @PostMapping("/create-with-customer/{customerId}/table/{tableId}")
    public ResponseEntity<?> createWithCustomerId(@PathVariable Long customerId, @PathVariable Long tableId, @RequestBody Order order) {
        User customer = userService.findById(customerId);
        if (customer == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Customer not found"));
        }

        RestaurantTable table = tableService.findById(tableId);
        if (table == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Table not found"));
        }

        order.setCustomer(customer);
        order.setTable(table);
        Order created = orderService.create(order);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Order created successfully",
                        "order", created
                )
        );
    }

    // Get all orders for staff dashboard (filtered: PENDING_PAYMENT, SERVED, etc.)
    @GetMapping("/list")
    public ResponseEntity<?> getAll() {
        List<Order> orders = orderService.getOrdersForStaffDashboard();
        return ResponseEntity.ok(
                Map.of(
                        "message", "Orders retrieved successfully",
                        "orders", orders
                )
        );
    }

    // Get current customer's orders
    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(@RequestParam(required = false) Long customerId) {
        if (customerId == null) {
            // Try to get from authentication context if available
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
                    String username = authentication.getName();
                    User currentUser = userService.findByUsername(username);
                    if (currentUser != null) {
                        customerId = currentUser.getId();
                    }
                }
            } catch (Exception e) {
                // Authentication not available, customerId must be provided
            }
        }

        if (customerId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Customer ID is required"));
        }

        List<Order> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Customer orders retrieved successfully",
                        "orders", orders
                )
        );
    }

    // Get order by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Order order = orderService.getById(id);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Order retrieved successfully",
                        "order", order
                )
        );
    }

    // Get orders by table
    @GetMapping("/table/{tableId}")
    public ResponseEntity<?> getByTable(@PathVariable Long tableId) {
        List<Order> orders = orderService.getOrdersByTable(tableId);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Orders retrieved successfully",
                        "orders", orders
                )
        );
    }

    // Get active orders by table (for staff to see current orders)
    @GetMapping("/table/{tableId}/active")
    public ResponseEntity<?> getActiveByTable(@PathVariable Long tableId) {
        List<Order> orders = orderService.getActiveOrdersByTable(tableId);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Active orders retrieved successfully",
                        "orders", orders
                )
        );
    }

    // Add items to order
    @PostMapping("/{orderId}/add-items")
    public ResponseEntity<?> addItems(@PathVariable Long orderId, @RequestBody List<OrderItem> items) {
        Order order = orderService.addItem(orderId, items);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Items added to order successfully",
                        "order", order
                )
        );
    }

    // Remove item from order
    @DeleteMapping("/{orderId}/remove-item/{itemId}")
    public ResponseEntity<?> removeItem(@PathVariable Long orderId, @PathVariable Long itemId) {
        Order order = orderService.removeItem(orderId, itemId);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Item removed from order successfully",
                        "order", order
                )
        );
    }

    // Update order item status (for kitchen staff)
    @PutMapping("/{orderId}/item/{itemId}/status/{status}")
    public ResponseEntity<?> updateItemStatus(@PathVariable Long orderId,
                                            @PathVariable Long itemId,
                                            @PathVariable String status) {
        Order order = orderService.updateItemStatus(orderId, itemId, status);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Order item status updated successfully",
                        "order", order
                )
        );
    }

    // Update order status
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @PathVariable String status) {
        Order order = orderService.getById(id);
        order.setStatus(status);
        Order updated = orderService.update(id, order);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Order status updated successfully",
                        "order", updated
                )
        );
    }

    // Checkout order
    @PutMapping("/{orderId}/checkout")
    public ResponseEntity<?> checkout(@PathVariable Long orderId) {
        Order order = orderService.checkout(orderId);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Order checked out successfully",
                        "order", order
                )
        );
    }

    // Delete order
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.ok(
                Map.of("message", "Order deleted successfully")
        );
    }

    // NEW FLOW: Get or create active order for table
    @PostMapping("/table/{tableId}/get-or-create")
    public ResponseEntity<?> getOrCreateActiveOrder(@PathVariable Long tableId, 
                                                     @RequestParam(required = false) Long customerId) {
        try {
            Order order = orderService.getOrCreateActiveOrder(tableId, customerId);
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Active order retrieved/created successfully",
                            "order", order
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    // NEW FLOW: Add items to active order for table
    @PostMapping("/table/{tableId}/add-items")
    public ResponseEntity<?> addItemsToTable(@PathVariable Long tableId,
                                              @RequestParam(required = false) Long customerId,
                                              @RequestBody List<OrderItem> items) {
        System.out.println("==============================================");
        System.out.println("📥 POST /api/orders/table/" + tableId + "/add-items");
        System.out.println("   CustomerId: " + customerId);
        System.out.println("   Items count: " + (items != null ? items.size() : "null"));
        if (items != null) {
            for (OrderItem item : items) {
                System.out.println("   - " + (item.getMenuItem() != null ? item.getMenuItem().getName() : "Unknown") + " x" + item.getQuantity());
            }
        }
        System.out.println("==============================================");
        
        try {
            System.out.println("✅ Calling orderService.addItemsToActiveOrder()...");
            Order order = orderService.addItemsToActiveOrder(tableId, customerId, items);
            System.out.println("✅ Order created/updated: #" + order.getId());
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Items added successfully",
                            "order", order
                    )
            );
        } catch (Exception e) {
            System.err.println("❌ Error adding items: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    // NEW FLOW: Close order (when staff checks out table)
    @PutMapping("/{orderId}/close")
    public ResponseEntity<?> closeOrder(@PathVariable Long orderId) {
        try {
            Order order = orderService.closeOrder(orderId);
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Order closed successfully",
                            "order", order
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }
}
