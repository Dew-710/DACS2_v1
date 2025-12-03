package com.restaurant.backend.Repository;

import com.restaurant.backend.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByStaffId(Long staffId);

    List<Order> findByTableId(Long tableId);

    List<Order> findByBookingId(Long bookingId);

    List<Order> findByStatus(String status);

    @Query("SELECT o FROM Order o WHERE o.table.id = :tableId AND o.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Order> findActiveOrdersByTableId(@Param("tableId") Long tableId);
}
