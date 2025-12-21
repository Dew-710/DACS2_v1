package com.restaurant.backend.Repository;

import com.restaurant.backend.Entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerId(Long customerId);

    List<Booking> findByTable_Id(Long tableId);

    List<Booking> findByStatus(String status);

    @Query("SELECT b FROM Booking b WHERE b.table.id = :tableId AND b.date = :date AND b.status NOT IN ('CANCELLED')")
    List<Booking> findBookingsByTableAndDate(@Param("tableId") Long tableId, @Param("date") LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.table.id = :tableId AND b.date = :date " +
           "AND b.status IN ('CONFIRMED', 'PENDING_CHECKIN')")
    List<Booking> findBookingsOnDate(@Param("tableId") Long tableId,
                                   @Param("date") LocalDate date);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.customer LEFT JOIN FETCH b.table")
    List<Booking> findAllWithRelationships();
}
