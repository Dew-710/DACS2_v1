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

    List<Order> findByTable_Id(Long tableId);

    List<Order> findByBookingId(Long bookingId);

    List<Order> findByStatus(String status);

    // Find ACTIVE orders for table - orders that are still being served (not yet checked out)
    // PENDING_PAYMENT orders are NOT active (already checked out, waiting for payment)
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.menuItem WHERE o.table.id = :tableId AND (o.paymentStatus IS NULL OR o.paymentStatus != 'PAID') AND o.status NOT IN ('CANCELLED', 'PENDING_PAYMENT')")
    List<Order> findActiveOrdersByTableId(@Param("tableId") Long tableId);

    // Get orders for staff dashboard - show only PENDING_PAYMENT orders (ready for payment)
    // JOIN FETCH để load orderItems và menuItem cùng lúc (tránh N+1 query)
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.menuItem WHERE o.status = 'PENDING_PAYMENT' AND (o.paymentStatus IS NULL OR o.paymentStatus != 'PAID') ORDER BY o.updatedAt DESC")
    List<Order> findOrdersForStaffDashboard();
}
