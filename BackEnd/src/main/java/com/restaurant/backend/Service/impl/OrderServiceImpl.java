package com.restaurant.backend.Service.impl;

import com.restaurant.backend.Entity.MenuItem;
import com.restaurant.backend.Entity.Order;
import com.restaurant.backend.Entity.OrderItem;
import com.restaurant.backend.Entity.RestaurantTable;
import com.restaurant.backend.Entity.User;
import com.restaurant.backend.Repository.MenuItemRepository;
import com.restaurant.backend.Repository.OrderRepository;
import com.restaurant.backend.Repository.RestaurantTableRepository;
import com.restaurant.backend.Repository.UserRepository;
import com.restaurant.backend.Service.MenuItemService;
import com.restaurant.backend.Service.OrderService;
import com.restaurant.backend.Service.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemService menuItemService;
    private final RestaurantTableRepository tableRepository;
    private final UserRepository userRepository;
    private final TelegramBotService telegramBotService;
    private final com.restaurant.backend.websocket.IoTWebSocketHandler webSocketHandler;

    @Override
    public Order create(Order order) {
        LocalDateTime now = LocalDateTime.now();
        order.setOrderTime(now);
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(now);
        }
        if (order.getUpdatedAt() == null) {
            order.setUpdatedAt(now);
        }
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
        existingOrder.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(existingOrder);
    }

    @Override
    public void delete(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public Order addItem(Long orderId, List<OrderItem> items) {
        Order order = getById(orderId);
        
        // Calculate current round number (max round + 1)
        int currentRound = order.getOrderItems().stream()
                .map(OrderItem::getRoundNumber)
                .filter(r -> r != null)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        
        log.info("✅ Adding {} items to order #{}, Round #{}", items.size(), orderId, currentRound);
        
        // Prepare notification message
        StringBuilder itemsDescription = new StringBuilder();
        
        for (OrderItem item : items) {
            // Load menuItem if not already set but menuItemId is provided
            if (item.getMenuItem() == null) {
                // For now, we'll assume menuItem is provided in the request
                // In a real implementation, you might need to add menuItemId field to OrderItem
                throw new IllegalArgumentException("MenuItem must be provided");
            }
            item.setOrder(order);
            item.setStatus("PENDING");
            item.setRoundNumber(currentRound); // Set lượt gọi món
            item.setIsConfirmed(false); // Chưa confirm, chưa tính tiền
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            
            // Build notification description
            itemsDescription.append("   • ")
                    .append(item.getMenuItem().getName())
                    .append(" x").append(item.getQuantity());
            if (item.getNotes() != null && !item.getNotes().isEmpty()) {
                itemsDescription.append(" (").append(item.getNotes()).append(")");
            }
            itemsDescription.append("\n");
        }
        
        order.getOrderItems().addAll(items);
        
        // DON'T calculate total yet - only confirmed items count toward total
        order.setUpdatedAt(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        log.info("✅ Saved order #{} with {} new items (Round {}, Draft mode)", orderId, items.size(), currentRound);
        
        // Send Telegram notification about new items WITH ROUND NUMBER
        try {
            String roundLabel = "LƯỢT " + currentRound;
            log.info("🔔 Calling Telegram service for Round {}", currentRound);
            telegramBotService.sendNewItemsNotification(updatedOrder, roundLabel + "\n" + itemsDescription.toString());
            log.info("✅ Telegram notification call completed");
        } catch (Exception e) {
            log.error("❌ Failed to send Telegram notification: {}", e.getMessage(), e);
        }
        
        // WebSocket notification
        if (updatedOrder.getTable() != null) {
            try {
                webSocketHandler.notifyNewOrder(
                        updatedOrder.getTable().getTableName(), 
                        "Lượt " + currentRound + " - Order #" + updatedOrder.getId()
                );
            } catch (Exception e) {
                log.error("Failed to send WebSocket notification: {}", e.getMessage());
            }
        }
        
        return updatedOrder;
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
        order.setPaymentStatus("PAID"); // Use payment_status for payment tracking
        order.setStatus("PENDING_PAYMENT"); // Chờ thanh toán, hiển thị cho staff
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
        return orderRepository.findByTable_Id(tableId);
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
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        // Notify status change if different from old status
        if (!status.equals(oldStatus)) {
            // Send Telegram notification
            try {
                telegramBotService.sendOrderStatusUpdate(updatedOrder, oldStatus, status);
            } catch (Exception e) {
                log.error("Failed to send Telegram notification: {}", e.getMessage());
            }
            
            // WebSocket notification
            if (updatedOrder.getTable() != null) {
                String tableName = updatedOrder.getTable().getTableName();
                webSocketHandler.notifyOrderStatusUpdate(tableName, status);
            }
        }

        return updatedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatusForKitchen(String status) {
        return orderRepository.findByStatus(status);
    }

    private void calculateTotalAmount(Order order) {
        // CHỈ tính tổng tiền cho items đã CONFIRMED (isConfirmed = true)
        // Items chưa confirmed (draft) KHÔNG được tính vào tổng tiền
        BigDecimal total = order.getOrderItems().stream()
                .filter(item -> !"CANCELLED".equals(item.getStatus()))
                .filter(item -> Boolean.TRUE.equals(item.getIsConfirmed())) // CHỈ tính confirmed items
                .map(item -> {
                    // Calculate subtotal as quantity * price
                    if (item.getSubtotal() != null) {
                        return item.getSubtotal();
                    } else {
                        return item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
        log.debug("Calculated total amount: {} (confirmed items only)", total);
    }

    @Override
    public Order getOrCreateActiveOrder(Long tableId, Long customerId) {
        // Find active order for this table
        List<Order> activeOrders = orderRepository.findActiveOrdersByTableId(tableId);
        
        if (!activeOrders.isEmpty()) {
            // Return existing active order
            Order existingOrder = activeOrders.get(0);
            log.info("Found existing active order #{} for table {}", existingOrder.getId(), tableId);
            return existingOrder;
        }
        
        // Create new order if no active order exists
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found with id: " + tableId));
        
        User customer = null;
        if (customerId != null) {
            customer = userRepository.findById(customerId).orElse(null);
        }
        
        Order newOrder = new Order();
        newOrder.setTable(table);
        newOrder.setCustomer(customer);
        newOrder.setStatus("ACTIVE");
        newOrder.setOrderTime(LocalDateTime.now());
        newOrder.setCreatedAt(LocalDateTime.now());
        newOrder.setUpdatedAt(LocalDateTime.now());
        newOrder.setTotalAmount(BigDecimal.ZERO);
        newOrder.setOrderItems(new ArrayList<>());
        
        Order savedOrder = orderRepository.save(newOrder);
        log.info("Created new active order #{} for table {}", savedOrder.getId(), tableId);
        
        // Send Telegram notification
        try {
            telegramBotService.sendOrderNotification(savedOrder, "ORDER MỚI");
        } catch (Exception e) {
            log.error("Failed to send Telegram notification: {}", e.getMessage());
        }
        
        // WebSocket notification
        if (table != null) {
            webSocketHandler.notifyNewOrder(table.getTableName(), "Order #" + savedOrder.getId());
        }
        
        return savedOrder;
    }

    @Override
    public Order addItemsToActiveOrder(Long tableId, Long customerId, List<OrderItem> items) {
        // Get or create active order
        Order order = getOrCreateActiveOrder(tableId, customerId);
        
        // Calculate current round number (max round + 1)
        int currentRound = order.getOrderItems().stream()
                .map(OrderItem::getRoundNumber)
                .filter(r -> r != null)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        
        log.info("Adding items to order #{}, Round #{}", order.getId(), currentRound);
        
        // Prepare notification message
        StringBuilder itemsDescription = new StringBuilder();
        
        // Add items to order
        for (OrderItem item : items) {
            if (item.getMenuItem() == null) {
                throw new IllegalArgumentException("MenuItem must be provided for each item");
            }
            
            item.setOrder(order);
            item.setStatus("PENDING");
            item.setRoundNumber(currentRound); // Set lượt gọi món
            item.setIsConfirmed(false); // Chưa confirm, chưa tính tiền
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            
            // Set price from menu item if not set
            if (item.getPrice() == null) {
                item.setPrice(item.getMenuItem().getPrice());
            }
            
            order.getOrderItems().add(item);
            
            // Build notification description
            itemsDescription.append("   • ")
                    .append(item.getMenuItem().getName())
                    .append(" x").append(item.getQuantity());
            if (item.getNotes() != null && !item.getNotes().isEmpty()) {
                itemsDescription.append(" (").append(item.getNotes()).append(")");
            }
            itemsDescription.append("\n");
        }
        
        // DON'T calculate total yet - only confirmed items count toward total
        order.setUpdatedAt(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        log.info("✅ Added {} items to order #{} (Round {}, Draft mode)", items.size(), updatedOrder.getId(), currentRound);
        
        // Send Telegram notification about new items WITH ROUND NUMBER
        try {
            String roundLabel = "LƯỢT " + currentRound;
            log.info("🔔 Calling Telegram service to send notification for Round {}", currentRound);
            telegramBotService.sendNewItemsNotification(updatedOrder, roundLabel + "\n" + itemsDescription.toString());
            log.info("✅ Telegram notification call completed");
        } catch (Exception e) {
            log.error("❌ Failed to send Telegram notification: {}", e.getMessage(), e);
        }
        
        // WebSocket notification
        if (updatedOrder.getTable() != null) {
            webSocketHandler.notifyNewOrder(
                    updatedOrder.getTable().getTableName(), 
                    "Lượt " + currentRound + " - Order #" + updatedOrder.getId()
            );
        }
        
        return updatedOrder;
    }

    @Override
    public Order closeOrder(Long orderId) {
        Order order = getById(orderId);
        
        // QUAN TRỌNG: Confirm tất cả items (chuyển từ draft sang confirmed)
        int confirmedCount = 0;
        for (OrderItem item : order.getOrderItems()) {
            if (Boolean.FALSE.equals(item.getIsConfirmed())) {
                item.setIsConfirmed(true); // Confirm item để tính tiền
                item.setUpdatedAt(LocalDateTime.now());
                confirmedCount++;
            }
        }
        
        log.info("Confirmed {} draft items for order #{}", confirmedCount, orderId);
        
        // Recalculate total amount AFTER confirming all items
        calculateTotalAmount(order);
        
        String oldPaymentStatus = order.getPaymentStatus();
        boolean wasAlreadyPaid = "PAID".equals(oldPaymentStatus);
        
        // QUAN TRỌNG: Khi checkout bàn, payment_status phải để NULL (chưa thanh toán)
        // Chỉ khi thanh toán xong (PayOS webhook hoặc cash) mới set payment_status = 'PAID'
        // KHÔNG set payment_status ở đây!
        
        // Set status to PENDING_PAYMENT so staff can see it in dashboard (chờ thanh toán)
        order.setStatus("PENDING_PAYMENT");
        order.setUpdatedAt(LocalDateTime.now());
        
        Order closedOrder = orderRepository.save(order);
        log.info("Closed order #{} with total amount: {} (confirmed {} items, payment_status: {})", 
                orderId, closedOrder.getTotalAmount(), confirmedCount, closedOrder.getPaymentStatus());
        
        // Send Telegram notification ONLY if order wasn't already PAID (prevent duplicates)
        if (!wasAlreadyPaid) {
            try {
                log.info("📤 Sending checkout notification to Telegram for order #{}", orderId);
                telegramBotService.sendCheckoutNotification(
                        closedOrder, 
                        closedOrder.getTotalAmount().doubleValue()
                );
            } catch (Exception e) {
                log.error("Failed to send Telegram notification: {}", e.getMessage());
            }
        } else {
            log.info("⏭️ Skipping Telegram notification for order #{} (already paid before)", orderId);
        }
        
        // WebSocket notification
        if (closedOrder.getTable() != null) {
            webSocketHandler.notifyOrderStatusUpdate(
                    closedOrder.getTable().getTableName(), 
                    "PAID"
            );
        }
        
        return closedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersForStaffDashboard() {
        return orderRepository.findOrdersForStaffDashboard();
    }
}
