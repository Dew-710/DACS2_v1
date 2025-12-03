package com.restaurant.backend.Service.impl;

import com.restaurant.backend.Entity.Order;
import com.restaurant.backend.Entity.OrderItem;
import com.restaurant.backend.Repository.OrderRepository;
import com.restaurant.backend.Service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final com.restaurant.backend.websocket.IoTWebSocketHandler webSocketHandler;

    @Override
    public Order create(Order order) {
        order.setOrderTime(LocalDateTime.now());
        if (order.getStatus() == null) {
            order.setStatus("PLACED");
        }

        Order savedOrder = orderRepository.save(order);

        // Notify kitchen and staff about new order
        if (savedOrder.getTable() != null) {
            String tableName = savedOrder.getTable().getTableName();
            String orderDetails = "Order #" + savedOrder.getId();
            webSocketHandler.notifyNewOrder(tableName, orderDetails);
        }

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order update(Long id, Order order) {
        Order existingOrder = getById(id);
        existingOrder.setStatus(order.getStatus());
        existingOrder.setTotalAmount(order.getTotalAmount());
        return orderRepository.save(existingOrder);
    }

    @Override
    public void delete(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public Order addItem(Long orderId, List<OrderItem> items) {
        Order order = getById(orderId);
        for (OrderItem item : items) {
            item.setOrder(order);
            item.setStatus("PENDING");
            // Calculate subtotal
            item.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.getOrderItems().addAll(items);
        calculateTotalAmount(order);
        return orderRepository.save(order);
    }

    @Override
    public Order removeItem(Long orderId, Long itemId) {
        Order order = getById(orderId);
        order.getOrderItems().removeIf(item -> item.getId().equals(itemId));
        calculateTotalAmount(order);
        return orderRepository.save(order);
    }

    @Override
    public Order updateItemStatus(Long orderId, Long itemId, String status) {
        Order order = getById(orderId);
        order.getOrderItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> item.setStatus(status));
        return orderRepository.save(order);
    }

    @Override
    public Order checkout(Long orderId) {
        Order order = getById(orderId);
        order.setStatus("COMPLETED");
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStaff(Long staffId) {
        return orderRepository.findByStaffId(staffId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByTable(Long tableId) {
        return orderRepository.findByTableId(tableId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByBooking(Long bookingId) {
        return orderRepository.findByBookingId(bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getActiveOrdersByTable(Long tableId) {
        return orderRepository.findActiveOrdersByTableId(tableId);
    }

    @Override
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = getById(orderId);
        String oldStatus = order.getStatus();
        order.setStatus(status);

        Order updatedOrder = orderRepository.save(order);

        // Notify status change if different from old status
        if (!status.equals(oldStatus) && updatedOrder.getTable() != null) {
            String tableName = updatedOrder.getTable().getTableName();
            webSocketHandler.notifyOrderStatusUpdate(tableName, status);
        }

        return updatedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatusForKitchen(String status) {
        return orderRepository.findByStatus(status);
    }

    private void calculateTotalAmount(Order order) {
        BigDecimal total = order.getOrderItems().stream()
                .filter(item -> !"CANCELLED".equals(item.getStatus()))
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
    }
}
