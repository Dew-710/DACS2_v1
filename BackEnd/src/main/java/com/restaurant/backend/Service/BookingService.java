package com.restaurant.backend.Service;

import com.restaurant.backend.Dto.Request.BookingRequest;
import com.restaurant.backend.Entity.Booking;
import com.restaurant.backend.Entity.User;

import java.util.List;

public interface BookingService {

    List<Booking> getAllBookings();

    List<Booking> findAll();

    Booking findById(Long id);



    Booking createBooking(Booking booking);

    Booking updateBooking(Long id, Booking booking);



    Booking getBookingById(long id);
    Booking getBookingByCustomerId(long customerId);

    Booking assignTable(Long id, Long tableId);

    void deleteBooking(Long id);

    Booking updateStatus(Long id, String confirmed);
}
