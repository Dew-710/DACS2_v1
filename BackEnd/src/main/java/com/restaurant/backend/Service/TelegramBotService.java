package com.restaurant.backend.Service;

import com.restaurant.backend.Entity.Order;
import com.restaurant.backend.Entity.OrderItem;

public interface TelegramBotService {
    
    /**
     * Send order notification to kitchen via Telegram
     */
    void sendOrderNotification(Order order, String action);
    
    /**
     * Send new order items notification
     */
    void sendNewItemsNotification(Order order, String itemsDescription);
    
    /**
     * Send order status update
     */
    void sendOrderStatusUpdate(Order order, String oldStatus, String newStatus);
    
    /**
     * Send order checkout notification
     */
    void sendCheckoutNotification(Order order, double totalAmount);
}





