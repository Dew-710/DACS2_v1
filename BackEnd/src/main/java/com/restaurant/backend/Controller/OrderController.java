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

/**
 * Controller quản lý các API endpoint liên quan đến đơn hàng (Order)
 * 
 * Chức năng chính:
 * - Tạo đơn hàng mới (có booking hoặc walk-in)
 * - Thêm/xóa món ăn vào đơn hàng
 * - Quản lý trạng thái đơn hàng (PLACED, PENDING_PAYMENT, PAID, etc.)
 * - Checkout đơn hàng (chuyển sang chờ thanh toán)
 * - Lấy danh sách đơn hàng cho staff dashboard và customer dashboard
 */
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

    /**
     * Tạo đơn hàng mới cho một bàn
     * Endpoint này dùng để tạo đơn hàng đơn giản, không có booking
     * 
     * @param order Đối tượng Order chứa thông tin đơn hàng
     * @return Đơn hàng đã được tạo thành công
     */
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

    /**
     * Tạo đơn hàng mới từ OrderRequest
     * 
     * Endpoint này hỗ trợ 2 trường hợp:
     * 1. Đơn hàng từ booking: Có bookingId → LUÔN LUÔN dùng customer từ booking (đảm bảo đúng khách đã đặt bàn)
     * 2. Đơn hàng walk-in: Không có bookingId → customerId có thể null hoặc từ request
     * 
     * Logic quan trọng:
     * - Nếu có bookingId: Ưu tiên dùng customer từ booking, bỏ qua customerId trong request
     * - Nếu không có bookingId: Dùng customerId từ request (có thể null cho walk-in)
     * 
     * @param request OrderRequest chứa thông tin: tableId, customerId (optional), bookingId (optional), staffId (optional)
     * @return Đơn hàng đã được tạo thành công
     */
    @PostMapping("/create-from-request")
    public ResponseEntity<?> createFromRequest(@RequestBody OrderRequest request) {
        System.out.println("📥 Received create-from-request");
        System.out.println("   Request: " + request);
        System.out.println("   TableId: " + request.getTableId());
        System.out.println("   CustomerId: " + request.getCustomerId());
        System.out.println("   BookingId: " + request.getBookingId());
        System.out.println("   Status: " + request.getStatus());
        System.out.println("   Items: " + (request.getItems() != null ? request.getItems().size() : "null"));
        
        // Kiểm tra bàn có tồn tại không
        if (request.getTableId() == null) {
            System.err.println("❌ Table ID is null");
            return ResponseEntity.badRequest().body(Map.of("message", "Table ID is required"));
        }

        RestaurantTable table = tableService.findById(request.getTableId());
        if (table == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Table not found"));
        }

        // Xử lý booking và customer:
        // QUAN TRỌNG: Nếu có bookingId, LUÔN LUÔN dùng customer từ booking (đảm bảo đúng khách đã đặt bàn)
        // Nếu không có bookingId, mới dùng customerId từ request (có thể null cho walk-in)
        Booking booking = null;
        User customer = null;
        
        if (request.getBookingId() != null) {
            // Trường hợp 1: Có booking → Lấy customer từ booking
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
            // Trường hợp 2: Không có booking → Dùng customerId từ request (nếu có)
            if (request.getCustomerId() != null) {
                customer = userService.findById(request.getCustomerId());
                if (customer == null) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Customer not found"));
                }
            }
            // customer có thể null nếu walk-in (không có booking, không có customerId)
        }

        // Xử lý staff (nếu có)
        User staff = null;
        if (request.getStaffId() != null) {
            staff = userService.findById(request.getStaffId());
            if (staff == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Staff not found"));
            }
        }

        // Tạo đối tượng Order từ thông tin đã xử lý
        Order order = Order.builder()
                .customer(customer)  // Có thể null nếu walk-in (khách vãng lai)
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

    /**
     * Tạo đơn hàng mới với customer ID và table ID (dùng cho check-in)
     * 
     * Endpoint này được dùng khi khách hàng check-in vào bàn và cần tạo đơn hàng ngay
     * 
     * @param customerId ID của khách hàng
     * @param tableId ID của bàn
     * @param order Đối tượng Order chứa thông tin bổ sung
     * @return Đơn hàng đã được tạo thành công
     */
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

    /**
     * Lấy danh sách đơn hàng cho staff dashboard
     * 
     * Endpoint này trả về các đơn hàng có trạng thái PENDING_PAYMENT (chờ thanh toán)
     * để staff có thể xem và xử lý thanh toán
     * 
     * @return Danh sách đơn hàng đang chờ thanh toán
     */
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

    /**
     * Lấy danh sách đơn hàng của khách hàng hiện tại
     * 
     * Endpoint này dùng để hiển thị lịch sử đơn hàng trong customer dashboard
     * 
     * Logic:
     * - Nếu có customerId trong request → dùng customerId đó
     * - Nếu không có → thử lấy từ authentication context (nếu user đã đăng nhập)
     * - Nếu vẫn không có → trả về lỗi yêu cầu customerId
     * 
     * @param customerId ID của khách hàng (optional, có thể lấy từ authentication)
     * @return Danh sách đơn hàng của khách hàng
     */
    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(@RequestParam(required = false) Long customerId) {
        if (customerId == null) {
            // Thử lấy customerId từ authentication context nếu user đã đăng nhập
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
                // Authentication không khả dụng, phải cung cấp customerId trong request
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

    /**
     * Lấy thông tin đơn hàng theo ID
     * 
     * @param id ID của đơn hàng
     * @return Thông tin đơn hàng
     */
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

    /**
     * Lấy tất cả đơn hàng của một bàn (bao gồm cả đã thanh toán và chưa thanh toán)
     * 
     * @param tableId ID của bàn
     * @return Danh sách tất cả đơn hàng của bàn
     */
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

    /**
     * Lấy các đơn hàng đang hoạt động của một bàn (chưa thanh toán, chưa hủy)
     * 
     * Endpoint này dùng để staff xem các đơn hàng hiện tại của bàn
     * Chỉ trả về các đơn hàng có paymentStatus != 'PAID' và status != 'CANCELLED', 'PENDING_PAYMENT'
     * 
     * @param tableId ID của bàn
     * @return Danh sách đơn hàng đang hoạt động
     */
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

    /**
     * Thêm món ăn vào đơn hàng
     * 
     * Khi thêm món, hệ thống sẽ:
     * - Tự động tính round_number (lượt gọi món)
     * - Set is_confirmed = false (chưa xác nhận, chưa tính tiền)
     * - Gửi thông báo Telegram cho bếp
     * 
     * @param orderId ID của đơn hàng
     * @param items Danh sách món ăn cần thêm
     * @return Đơn hàng đã được cập nhật
     */
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

    /**
     * Xóa món ăn khỏi đơn hàng
     * 
     * @param orderId ID của đơn hàng
     * @param itemId ID của món ăn cần xóa
     * @return Đơn hàng đã được cập nhật
     */
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

    /**
     * Cập nhật trạng thái món ăn (dùng cho bếp)
     * 
     * Các trạng thái có thể: PENDING, PREPARING, READY, SERVED, CANCELLED
     * 
     * @param orderId ID của đơn hàng
     * @param itemId ID của món ăn
     * @param status Trạng thái mới
     * @return Đơn hàng đã được cập nhật
     */
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

    /**
     * Cập nhật trạng thái đơn hàng
     * 
     * Các trạng thái có thể: PLACED, CONFIRMED, PREPARING, READY, SERVED, PENDING_PAYMENT, PAID, CANCELLED
     * 
     * @param id ID của đơn hàng
     * @param status Trạng thái mới
     * @return Đơn hàng đã được cập nhật
     */
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

    /**
     * Checkout đơn hàng (đóng đơn hàng, chuyển sang chờ thanh toán)
     * 
     * Lưu ý: Endpoint này đã được thay thế bởi closeOrder() trong OrderService
     * 
     * @param orderId ID của đơn hàng
     * @return Đơn hàng đã được checkout
     */
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

    /**
     * Xóa đơn hàng
     * 
     * @param id ID của đơn hàng cần xóa
     * @return Thông báo xóa thành công
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.ok(
                Map.of("message", "Order deleted successfully")
        );
    }

    /**
     * Lấy hoặc tạo đơn hàng đang hoạt động cho bàn
     * 
     * Endpoint này dùng cho flow mới:
     * - Nếu bàn đã có đơn hàng đang hoạt động → trả về đơn hàng đó
     * - Nếu chưa có → tạo đơn hàng mới với status = "ACTIVE"
     * 
     * @param tableId ID của bàn
     * @param customerId ID của khách hàng (optional)
     * @return Đơn hàng đang hoạt động (có sẵn hoặc mới tạo)
     */
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

    /**
     * Thêm món ăn vào đơn hàng đang hoạt động của bàn (Flow mới)
     * 
     * Endpoint này tự động:
     * - Tìm hoặc tạo đơn hàng đang hoạt động cho bàn
     * - Thêm món ăn vào đơn hàng đó
     * - Tự động tính round_number (lượt gọi món)
     * - Set is_confirmed = false (chưa xác nhận)
     * - Gửi thông báo Telegram cho bếp
     * 
     * @param tableId ID của bàn
     * @param customerId ID của khách hàng (optional)
     * @param items Danh sách món ăn cần thêm
     * @return Đơn hàng đã được cập nhật
     */
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

    /**
     * Đóng đơn hàng (khi staff checkout bàn)
     * 
     * Khi đóng đơn hàng, hệ thống sẽ:
     * - Confirm tất cả items (is_confirmed = true) để tính tiền
     * - Tính lại total_amount (chỉ tính items đã confirmed)
     * - Set status = "PENDING_PAYMENT" (chờ thanh toán)
     * - Set payment_status = NULL (chưa thanh toán, chỉ set "PAID" khi thanh toán xong)
     * - Gửi thông báo Telegram về tổng thanh toán
     * 
     * @param orderId ID của đơn hàng cần đóng
     * @return Đơn hàng đã được đóng
     */
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
