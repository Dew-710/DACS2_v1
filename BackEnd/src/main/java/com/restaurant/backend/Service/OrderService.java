package com.restaurant.backend.Service;

import com.restaurant.backend.Entity.Order;
import com.restaurant.backend.Entity.OrderItem;

import java.util.List;

public interface OrderService {

    // Basic CRUD operations
    Order create(Order order);
    Order getById(Long id);
    List<Order> getAll();
    Order update(Long id, Order order);
    void delete(Long id);

    // Business operations
    Order addItem(Long orderId, List<OrderItem> items);
    Order removeItem(Long orderId, Long itemId);
    Order updateItemStatus(Long orderId, Long itemId, String status);
    Order checkout(Long orderId);

    // Query operations
    List<Order> getOrdersByCustomer(Long customerId);
    List<Order> getOrdersByStaff(Long staffId);
    List<Order> getOrdersByTable(Long tableId);
    List<Order> getOrdersByBooking(Long bookingId);
    List<Order> getOrdersByStatus(String status);
    List<Order> getActiveOrdersByTable(Long tableId);

    // Kitchen workflow operations
    Order updateOrderStatus(Long orderId, String status);
    List<Order> getOrdersByStatusForKitchen(String status);
}
