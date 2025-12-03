package com.restaurant.backend.Service.impl;

import com.restaurant.backend.Entity.Booking;
import com.restaurant.backend.Entity.RestaurantTable;
import com.restaurant.backend.Repository.BookingRepository;
import com.restaurant.backend.Repository.RestaurantTableRepository;
import com.restaurant.backend.Service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RestaurantTableRepository restaurantTableRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Booking findById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    @Override
    public Booking createBooking(Booking booking) {
        // Validate table availability
        if (booking.getTable() != null) {
            validateTableAvailability(booking.getTable().getId(), booking.getDate(), booking.getTime());
        }

        if (booking.getStatus() == null) {
            booking.setStatus("PENDING");
        }

        return bookingRepository.save(booking);
    }

    @Override
    public Booking updateBooking(Long id, Booking booking) {
        Booking existingBooking = findById(id);
        existingBooking.setDate(booking.getDate());
        existingBooking.setTime(booking.getTime());
        existingBooking.setGuests(booking.getGuests());
        existingBooking.setNote(booking.getNote());
        existingBooking.setStatus(booking.getStatus());

        // Validate new table if changed
        if (booking.getTable() != null &&
            !booking.getTable().getId().equals(existingBooking.getTable().getId())) {
            validateTableAvailability(booking.getTable().getId(), booking.getDate(), booking.getTime());
            existingBooking.setTable(booking.getTable());
        }

        return bookingRepository.save(existingBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingById(long id) {
        return findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Booking getBookingByCustomerId(long customerId) {
        List<Booking> bookings = bookingRepository.findByCustomerId(customerId);
        return bookings.isEmpty() ? null : bookings.get(bookings.size() - 1);
    }

    @Override
    public Booking assignTable(Long id, Long tableId) {
        Booking booking = findById(id);
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found with id: " + tableId));

        validateTableAvailability(tableId, booking.getDate(), booking.getTime());

        booking.setTable(table);
        return bookingRepository.save(booking);
    }

    @Override
    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    @Override
    public Booking updateStatus(Long id, String status) {
        Booking booking = findById(id);
        booking.setStatus(status);

        // If confirmed, update table status to RESERVED
        if ("CONFIRMED".equals(status) && booking.getTable() != null) {
            booking.getTable().setStatus("RESERVED");
            restaurantTableRepository.save(booking.getTable());
        }

        return bookingRepository.save(booking);
    }

    // Additional methods for the improved system

    /**
     * Find available tables for a specific date and time
     */
    @Transactional(readOnly = true)
    public List<RestaurantTable> findAvailableTables(LocalDate date, LocalTime time, int guests) {
        List<RestaurantTable> allTables = restaurantTableRepository.findAll();

        return allTables.stream()
                .filter(table -> isTableAvailable(table.getId(), date, time))
                .filter(table -> table.getCapacity() >= guests)
                .collect(Collectors.toList());
    }

    /**
     * Suggest suitable tables based on guest count
     */
    @Transactional(readOnly = true)
    public List<RestaurantTable> suggestTables(int guests) {
        return restaurantTableRepository.findAll().stream()
                .filter(table -> "VACANT".equals(table.getStatus()))
                .filter(table -> table.getCapacity() >= guests)
                .sorted((t1, t2) -> Integer.compare(t1.getCapacity(), t2.getCapacity())) // Prefer smaller suitable tables
                .collect(Collectors.toList());
    }

    /**
     * Check if a table is available at specific date and time
     */
    @Transactional(readOnly = true)
    public boolean isTableAvailable(Long tableId, LocalDate date, LocalTime time) {
        // Assume 2-hour booking duration
        LocalTime endTime = time.plusHours(2);
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(tableId, date, time, endTime);
        return conflictingBookings.isEmpty();
    }

    /**
     * Validate table availability before booking
     */
    private void validateTableAvailability(Long tableId, LocalDate date, LocalTime time) {
        if (!isTableAvailable(tableId, date, time)) {
            throw new RuntimeException("Table is not available at the requested date and time");
        }
    }

    /**
     * Get booking by booking code (for check-in)
     */
    @Transactional(readOnly = true)
    public Booking getBookingByCode(String bookingCode) {
        // Assuming we add a bookingCode field to Booking entity
        // For now, return null - this would need database changes
        return null;
    }
}
