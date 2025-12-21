package com.restaurant.backend.Controller;

import com.restaurant.backend.Entity.Booking;
import com.restaurant.backend.Entity.RestaurantTable;
import com.restaurant.backend.Entity.User;
import com.restaurant.backend.Service.BookingService;
import com.restaurant.backend.Service.RestaurantTableService;
import com.restaurant.backend.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final com.restaurant.backend.Service.RestaurantTableService tableService;
    private final UserService userService;

    public BookingController(BookingService bookingService,
                           com.restaurant.backend.Service.RestaurantTableService tableService,
                           UserService userService) {
        this.bookingService = bookingService;
        this.tableService = tableService;
        this.userService = userService;
    }

    // Tạo booking với kiểm tra tính khả dụng của bàn
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> bookingData) {
        try {
            System.out.println("Creating booking with data: " + bookingData);

            // Tạo object booking từ dữ liệu request
            Booking booking = new Booking();
            booking.setCustomer(new User());
            booking.getCustomer().setId(Long.valueOf(bookingData.get("customerId").toString()));

            if (bookingData.containsKey("tableId") && bookingData.get("tableId") != null) {
                Long tableId = Long.valueOf(bookingData.get("tableId").toString());
                System.out.println("Setting table ID: " + tableId);

                // Lấy thông tin bàn thực tế từ database
                RestaurantTable table = tableService.findById(tableId);
                if (table == null) {
                    throw new RuntimeException("Table not found with id: " + tableId);
                }
                booking.setTable(table);
                System.out.println("Table set successfully: " + table.getTableName());
            } else {
                System.out.println("No tableId provided in booking data");
                throw new RuntimeException("Table ID is required for booking");
            }

            booking.setDate(java.time.LocalDate.parse(bookingData.get("date").toString()));
            booking.setTime(java.time.LocalTime.parse(bookingData.get("time").toString()));
            booking.setGuests(Integer.valueOf(bookingData.get("guests").toString()));

            if (bookingData.containsKey("notes") && bookingData.get("notes") != null) {
                booking.setNote(bookingData.get("notes").toString());
            }

            Booking created = bookingService.createBooking(booking);
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Booking created successfully",
                            "booking", created,
                            "bookingCode", "BK" + created.getId() // Simple booking code
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Failed to create booking: " + e.getMessage())
            );
        }
    }

    // Kiểm tra tính khả dụng của bàn cho ngày và giờ cụ thể
    @GetMapping("/availability")
    public ResponseEntity<?> checkAvailability(@RequestParam java.time.LocalDate date,
                                             @RequestParam java.time.LocalTime time,
                                             @RequestParam int guests) {
        List<com.restaurant.backend.Entity.RestaurantTable> availableTables =
            ((com.restaurant.backend.Service.impl.BookingServiceImpl) bookingService)
                .findAvailableTables(date, time, guests);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Available tables retrieved",
                        "date", date,
                        "time", time,
                        "guests", guests,
                        "availableTables", availableTables
                )
        );
    }

    // Đề xuất bàn phù hợp cho đặt bàn
    @GetMapping("/suggest-tables")
    public ResponseEntity<?> suggestTables(@RequestParam int guests) {
        List<com.restaurant.backend.Entity.RestaurantTable> suggestedTables =
            ((com.restaurant.backend.Service.impl.BookingServiceImpl) bookingService)
                .suggestTables(guests);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Suggested tables for " + guests + " guests",
                        "suggestedTables", suggestedTables
                )
        );
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAll() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(
                Map.of(
                        "message", "Bookings retrieved successfully",
                        "bookings", bookings
                )
        );
    }

    // Get active booking for a table (CONFIRMED or CHECKED_IN)
    @GetMapping("/table/{tableId}/active")
    public ResponseEntity<?> getActiveBookingByTable(@PathVariable Long tableId) {
        List<Booking> bookings = bookingService.getBookingsByTable(tableId);
        
        // Find active booking (CONFIRMED or CHECKED_IN)
        Booking activeBooking = bookings.stream()
            .filter(b -> b.getStatus() != null && 
                        (b.getStatus().equals("CONFIRMED") || b.getStatus().equals("CHECKED_IN")))
            .findFirst()
            .orElse(null);

        if (activeBooking == null) {
            return ResponseEntity.ok(
                Map.of(
                    "message", "No active booking for this table",
                    "hasActiveBooking", false
                )
            );
        }

        return ResponseEntity.ok(
            Map.of(
                "message", "Active booking found",
                "hasActiveBooking", true,
                "booking", activeBooking
            )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Booking retrieved successfully",
                        "booking", booking
                )
        );
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Booking booking) {
        Booking updated = bookingService.updateBooking(id, booking);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Booking updated successfully",
                        "booking", updated
                )
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok(
                Map.of("message", "Booking deleted successfully")
        );
    }

    // ⭐ Nghiệp vụ thực tế
    @PutMapping("/{id}/assign-table/{tableId}")
    public ResponseEntity<?> assignTable(@PathVariable Long id, @PathVariable Long tableId) {
        Booking booking = bookingService.assignTable(id, tableId);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Table assigned successfully",
                        "booking", booking
                )
        );
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Long id) {
        Booking booking = bookingService.updateStatus(id, "CONFIRMED");
        return ResponseEntity.ok(
                Map.of(
                        "message", "Booking confirmed successfully",
                        "booking", booking
                )
        );
    }

    // Check-in booking (khách hàng đã đến, bàn chuyển thành OCCUPIED)
    @PutMapping("/{id}/checkin")
    public ResponseEntity<?> checkIn(@PathVariable Long id) {
        Booking booking = bookingService.checkInBooking(id);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Check-in successful, table is now occupied",
                        "booking", booking
                )
        );
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        Booking booking = bookingService.updateStatus(id, "CANCELLED");
        return ResponseEntity.ok(
                Map.of(
                        "message", "Booking canceled successfully",
                        "booking", booking
                )
        );
    }

    // Get current customer's bookings
    @GetMapping("/my-bookings")
    public ResponseEntity<?> getMyBookings(@RequestParam(required = false) Long customerId) {
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

        List<Booking> bookings = bookingService.getBookingsByCustomer(customerId);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Customer bookings retrieved successfully",
                        "bookings", bookings
                )
        );
    }
}
