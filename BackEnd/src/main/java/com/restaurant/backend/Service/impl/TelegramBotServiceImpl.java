package com.restaurant.backend.Service.impl;

import com.restaurant.backend.Entity.Order;
import com.restaurant.backend.Entity.OrderItem;
import com.restaurant.backend.Service.TelegramBotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class TelegramBotServiceImpl extends TelegramLongPollingBot implements TelegramBotService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username:RestaurantBot}")
    private String botUsername;

    @Value("${telegram.bot.chat-id:}")
    private String chatId;

    @Value("${telegram.bot.enabled:false}")
    private boolean enabled;

    private final DecimalFormat currencyFormat = new DecimalFormat("#,###");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Handle incoming messages if needed
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatIdFromMessage = update.getMessage().getChatId();
            
            log.info("Received message from chat ID {}: {}", chatIdFromMessage, messageText);
            
            // If user sends /start, send back their chat ID
            if (messageText.equals("/start")) {
                sendTelegramMessage(chatIdFromMessage.toString(), 
                    "✅ Bot đã kích hoạt!\n\n" +
                    "📱 Chat ID của bạn: " + chatIdFromMessage + "\n\n" +
                    "Sử dụng Chat ID này để cấu hình nhận thông báo order.");
            }
        }
    }

    @Override
    public void sendOrderNotification(Order order, String action) {
        if (!enabled || chatId == null || chatId.isEmpty()) {
            log.info("Telegram bot is disabled or chat ID not configured");
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("🔔 <b>").append(action.toUpperCase()).append("</b>\n\n");
        message.append("📋 <b>Order #").append(order.getId()).append("</b>\n");
        message.append("🪑 Bàn: <b>").append(order.getTable() != null ? order.getTable().getTableName() : "N/A").append("</b>\n");
        
        if (order.getCustomer() != null) {
            message.append("👤 Khách: ").append(order.getCustomer().getFullName()).append("\n");
        }
        
        message.append("⏰ Thời gian: ").append(order.getCreatedAt().format(dateTimeFormatter)).append("\n");
        message.append("📊 Trạng thái: <b>").append(formatStatus(order.getStatus())).append("</b>\n\n");
        
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            message.append("🍽️ <b>Món ăn:</b>\n");
            for (OrderItem item : order.getOrderItems()) {
                message.append("   • ").append(item.getMenuItem().getName())
                       .append(" x").append(item.getQuantity());
                if (item.getNotes() != null && !item.getNotes().isEmpty()) {
                    message.append(" (").append(item.getNotes()).append(")");
                }
                message.append("\n");
            }
        }
        
        message.append("\n💰 Tổng tiền: <b>").append(currencyFormat.format(order.getTotalAmount())).append(" VNĐ</b>");

        sendTelegramMessage(chatId, message.toString());
    }

    @Override
    public void sendNewItemsNotification(Order order, String itemsDescription) {
        log.info("=== TELEGRAM DEBUG ===");
        log.info("Enabled: {}", enabled);
        log.info("Chat ID: {}", chatId);
        log.info("Order ID: {}", order.getId());
        log.info("Items Description: {}", itemsDescription);
        
        if (!enabled || chatId == null || chatId.isEmpty()) {
            log.warn("❌ Telegram bot is disabled or chat ID not configured! enabled={}, chatId={}", enabled, chatId);
            return;
        }

        log.info("✅ Telegram bot is enabled and configured. Preparing message...");
        
        // Calculate draft total (all items including unconfirmed)
        double draftTotal = order.getOrderItems().stream()
                .filter(item -> !"CANCELLED".equals(item.getStatus()))
                .mapToDouble(item -> {
                    if (item.getSubtotal() != null) {
                        return item.getSubtotal().doubleValue();
                    } else if (item.getPrice() != null) {
                        return item.getPrice().doubleValue() * item.getQuantity();
                    }
                    return 0.0;
                })
                .sum();
        
        StringBuilder message = new StringBuilder();
        message.append("➕ <b>THÊM MÓN MỚI</b>\n\n");
        message.append("📋 Order #").append(order.getId()).append("\n");
        message.append("🪑 Bàn: <b>").append(order.getTable() != null ? order.getTable().getTableName() : "N/A").append("</b>\n\n");
        message.append("🍽️ <b>Món mới:</b>\n");
        message.append(itemsDescription);
        message.append("\n💰 <b>Tạm tính: ").append(currencyFormat.format(draftTotal)).append(" VNĐ</b>");
        message.append("\n<i>(Chưa tính tiền, chờ đóng bàn)</i>");

        log.info("📤 Sending Telegram message to chat ID: {}", chatId);
        sendTelegramMessage(chatId, message.toString());
    }

    @Override
    public void sendOrderStatusUpdate(Order order, String oldStatus, String newStatus) {
        if (!enabled || chatId == null || chatId.isEmpty()) {
            log.info("Telegram bot is disabled or chat ID not configured");
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("🔄 <b>CẬP NHẬT TRẠNG THÁI</b>\n\n");
        message.append("📋 Order #").append(order.getId()).append("\n");
        message.append("🪑 Bàn: <b>").append(order.getTable() != null ? order.getTable().getTableName() : "N/A").append("</b>\n\n");
        message.append("Trạng thái: ").append(formatStatus(oldStatus))
               .append(" ➡️ <b>").append(formatStatus(newStatus)).append("</b>");

        sendTelegramMessage(chatId, message.toString());
    }

    @Override
    public void sendCheckoutNotification(Order order, double totalAmount) {
        if (!enabled || chatId == null || chatId.isEmpty()) {
            log.info("Telegram bot is disabled or chat ID not configured");
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("✅ <b>THANH TOÁN</b>\n\n");
        message.append("📋 Order #").append(order.getId()).append("\n");
        message.append("🪑 Bàn: <b>").append(order.getTable() != null ? order.getTable().getTableName() : "N/A").append("</b>\n");
        
        if (order.getCustomer() != null) {
            message.append("👤 Khách: ").append(order.getCustomer().getFullName()).append("\n");
        }
        
        message.append("\n💵 <b>Tổng thanh toán: ").append(currencyFormat.format(totalAmount)).append(" VNĐ</b>\n");
        message.append("⏰ Thời gian: ").append(order.getUpdatedAt().format(dateTimeFormatter));

        sendTelegramMessage(chatId, message.toString());
    }

    private void sendTelegramMessage(String chatId, String text) {
        try {
            log.info("📤 Preparing to send Telegram message...");
            log.info("   Chat ID: {}", chatId);
            log.info("   Text length: {} chars", text != null ? text.length() : 0);
            
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(text);
            message.setParseMode("HTML");
            
            log.info("   Calling execute()...");
            var result = execute(message);
            log.info("✅ Telegram message sent successfully! Message ID: {}", result.getMessageId());
            log.info("   To chat ID: {}", chatId);
        } catch (TelegramApiException e) {
            log.error("❌ Failed to send Telegram message to chat ID: {}", chatId);
            log.error("   Error message: {}", e.getMessage());
            log.error("   Error details:", e);
        } catch (Exception e) {
            log.error("❌ Unexpected error sending Telegram message:", e);
        }
    }

    private String formatStatus(String status) {
        switch (status.toUpperCase()) {
            case "ACTIVE": return "🟢 Đang phục vụ";
            case "PENDING": return "🟡 Chờ xử lý";
            case "PREPARING": return "👨‍🍳 Đang chuẩn bị";
            case "READY": return "✅ Sẵn sàng";
            case "SERVED": return "🍽️ Đã phục vụ";
            case "COMPLETED": return "✅ Hoàn thành";
            case "PAID": return "💰 Đã thanh toán";
            case "CANCELLED": return "❌ Đã hủy";
            default: return status;
        }
    }
}



