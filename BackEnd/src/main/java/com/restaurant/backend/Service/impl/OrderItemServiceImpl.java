package com.restaurant.backend.Service.impl;

import com.restaurant.backend.Entity.OrderItem;
import com.restaurant.backend.Repository.OrderItemRepository;
import com.restaurant.backend.Service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;

    @Override
    public OrderItem create(OrderItem item) {
        if (item.getStatus() == null) {
            item.setStatus("PENDING");
        }
        return orderItemRepository.save(item);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItem getById(Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OrderItem not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> getAll() {
        return orderItemRepository.findAll();
    }

    @Override
    public OrderItem update(Long id, OrderItem item) {
        OrderItem existingItem = getById(id);
        existingItem.setQuantity(item.getQuantity());
        existingItem.setPrice(item.getPrice());
        existingItem.setNotes(item.getNotes());
        existingItem.setStatus(item.getStatus());
        // Subtotal is calculated automatically via getter method
        return orderItemRepository.save(existingItem);
    }

    @Override
    public void delete(Long id) {
        orderItemRepository.deleteById(id);
    }

    @Override
    public OrderItem updateStatus(Long id, String status) {
        OrderItem item = getById(id);
        item.setStatus(status);
        return orderItemRepository.save(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> getItemsByOrder(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> getItemsByMenuItem(Long menuItemId) {
        return orderItemRepository.findByMenuItemId(menuItemId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> getItemsByStatus(String status) {
        return orderItemRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> getActiveItemsByOrder(Long orderId) {
        return orderItemRepository.findActiveItemsByOrderId(orderId);
    }
}
