package com.restaurant.backend.Repository;

import com.restaurant.backend.Entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByMenuItemId(Long menuItemId);

    List<OrderItem> findByStatus(String status);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId AND oi.status != 'CANCELLED'")
    List<OrderItem> findActiveItemsByOrderId(@Param("orderId") Long orderId);
}
