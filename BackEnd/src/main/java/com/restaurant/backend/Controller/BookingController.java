package com.restaurant.backend.Controller;

import com.restaurant.backend.Entity.Booking;
import com.restaurant.backend.Service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final com.restaurant.backend.Service.RestaurantTableService tableService;

    public BookingController(BookingService bookingService,
                           com.restaurant.backend.Service.RestaurantTableService tableService) {
        this.bookingService = bookingService;
        this.tableService = tableService;
    }

    // Create booking with table availability check
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Booking booking) {
        Booking created = bookingService.createBooking(booking);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Booking created successfully",
                        "booking", created,
                        "bookingCode", "BK" + created.getId() // Simple booking code
                )
        );
    }

    // Check table availability for specific date and time
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

    // Suggest suitable tables for booking
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

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        Booking booking = bookingService.updateStatus(id, "CANCELED");
        return ResponseEntity.ok(
                Map.of(
                        "message", "Booking canceled successfully",
                        "booking", booking
                )
        );
    }
}
