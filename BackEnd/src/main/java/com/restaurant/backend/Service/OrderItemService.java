package com.restaurant.backend.Service;

import com.restaurant.backend.Entity.OrderItem;

import java.util.List;

public interface OrderItemService {

    // Basic CRUD operations
    OrderItem create(OrderItem item);
    OrderItem getById(Long id);
    List<OrderItem> getAll();
    OrderItem update(Long id, OrderItem item);
    void delete(Long id);

    // Business operations
    OrderItem updateStatus(Long id, String status);

    // Query operations
    List<OrderItem> getItemsByOrder(Long orderId);
    List<OrderItem> getItemsByMenuItem(Long menuItemId);
    List<OrderItem> getItemsByStatus(String status);
    List<OrderItem> getActiveItemsByOrder(Long orderId);
}
