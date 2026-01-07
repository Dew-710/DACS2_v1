# Telegram Bot Setup Guide

## 🤖 Tổng quan

Telegram Bot được tích hợp để gửi thông báo real-time về:
- Order mới từ khách hàng
- Thêm món vào order đang hoạt động
- Cập nhật trạng thái order
- Thanh toán/checkout

**Bot Token:** `8370737734:AAFKdJE_WqS2G4lVKQxT9jbzI2yfi59JEhQ`

---

## 📋 Bước 1: Lấy Chat ID

### Cách 1: Gửi tin nhắn trực tiếp cho bot

1. Mở Telegram và tìm bot của bạn (sử dụng username bot)
2. Gửi lệnh `/start`
3. Bot sẽ trả lời với Chat ID của bạn:
   ```
   ✅ Bot đã kích hoạt!
   
   📱 Chat ID của bạn: 123456789
   
   Sử dụng Chat ID này để cấu hình nhận thông báo order.
   ```

### Cách 2: Sử dụng API Telegram

```bash
# Thay YOUR_BOT_TOKEN bằng token của bạn
curl https://api.telegram.org/bot8370737734:AAFKdJE_WqS2G4lVKQxT9jbzI2yfi59JEhQ/getUpdates
```

Tìm field `"chat":{"id":123456789}` trong response.

### Cách 3: Thêm bot vào group

1. Tạo group Telegram mới (ví dụ: "Restaurant Kitchen")
2. Thêm bot vào group
3. Gửi tin nhắn bất kỳ trong group
4. Call API getUpdates để lấy Chat ID của group

---

## 🔧 Bước 2: Cấu hình Backend

### Option 1: Sử dụng Environment Variables (Khuyến nghị)

```bash
# Enable Telegram bot
export TELEGRAM_BOT_ENABLED=true

# Bot token (đã được set mặc định trong application.properties)
export TELEGRAM_BOT_TOKEN=8370737734:AAFKdJE_WqS2G4lVKQxT9jbzI2yfi59JEhQ

# Chat ID (bạn cần cung cấp sau khi lấy được)
export TELEGRAM_BOT_CHAT_ID=YOUR_CHAT_ID_HERE

# Optional: Username bot
export TELEGRAM_BOT_USERNAME=RestaurantKitchenBot
```

### Option 2: Cập nhật application.properties

```properties
telegram.bot.enabled=true
telegram.bot.token=8370737734:AAFKdJE_WqS2G4lVKQxT9jbzI2yfi59JEhQ
telegram.bot.chat-id=YOUR_CHAT_ID_HERE
telegram.bot.username=RestaurantKitchenBot
```

### Restart Backend

```bash
cd BackEnd
./mvnw spring-boot:run
```

---

## 📱 Các loại thông báo

### 1. Order Mới (New Order)

```
🔔 ORDER MỚI

📋 Order #123
🪑 Bàn: A5
👤 Khách: Nguyễn Văn A
⏰ Thời gian: 05/01/2026 14:30
📊 Trạng thái: 🟢 Đang phục vụ

🍽️ Món ăn:
   • Phở Bò x2
   • Cà phê đen x1 (Ít đường)

💰 Tổng tiền: 150,000 VNĐ
```

### 2. Thêm Món Mới (Add Items)

```
➕ THÊM MÓN MỚI

📋 Order #123
🪑 Bàn: A5

🍽️ Món mới:
   • Bánh mì x1
   • Nước cam x2

💰 Tổng tiền hiện tại: 200,000 VNĐ
```

### 3. Cập Nhật Trạng Thái (Status Update)

```
🔄 CẬP NHẬT TRẠNG THÁI

📋 Order #123
🪑 Bàn: A5

Trạng thái: 🟡 Chờ xử lý ➡️ 👨‍🍳 Đang chuẩn bị
```

### 4. Thanh Toán (Checkout)

```
✅ THANH TOÁN

📋 Order #123
🪑 Bàn: A5
👤 Khách: Nguyễn Văn A

💵 Tổng thanh toán: 200,000 VNĐ
⏰ Thời gian: 05/01/2026 15:45
```

---

## 🔄 Luồng Order Mới

### Cơ chế hoạt động:

1. **Mỗi bàn chỉ có 1 order ACTIVE tại một thời điểm**
2. **Khách có thể gọi thêm món** → Thêm vào order hiện tại
3. **Staff checkout bàn** → Đóng order (status = COMPLETED)
4. **Mở bàn mới** → Tạo order mới

### Ví dụ thực tế:

```
Timeline cho Bàn A5:

14:00 - Khách check-in
        → API: getOrCreateActiveOrder(tableId=5)
        → Tạo Order #123 (status: ACTIVE)
        → Telegram: "🔔 ORDER MỚI"

14:05 - Khách gọi món đầu tiên
        → API: addItemsToTableOrder(tableId=5, items=[Phở x2, Cà phê x1])
        → Thêm vào Order #123
        → Telegram: "➕ THÊM MÓN MỚI"

14:30 - Khách gọi thêm món
        → API: addItemsToTableOrder(tableId=5, items=[Bánh mì x1])
        → Thêm vào Order #123 (cùng order)
        → Telegram: "➕ THÊM MÓN MỚI"

15:00 - Staff cập nhật trạng thái
        → API: updateOrderStatus(orderId=123, status="PREPARING")
        → Telegram: "🔄 CẬP NHẬT TRẠNG THÁI"

15:45 - Khách yêu cầu thanh toán
        → API: closeOrder(orderId=123)
        → Order #123 status = COMPLETED
        → Telegram: "✅ THANH TOÁN"

16:00 - Bàn A5 sẵn sàng cho khách mới
        → Khách mới check-in
        → API: getOrCreateActiveOrder(tableId=5)
        → Tạo Order #124 (order mới, độc lập)
```

---

## 🔐 Security & Best Practices

### 1. Bảo mật Bot Token

**QUAN TRỌNG:** Token bot là thông tin nhạy cảm!

```bash
# ✅ ĐÚNG: Dùng environment variables
export TELEGRAM_BOT_TOKEN=your_token_here

# ❌ SAI: Hardcode trong code
telegram.bot.token=8370737734:AAFKdJE_WqS2G4lVKQxT9jbzI2yfi59JEhQ
```

### 2. Giới hạn Chat ID

Bot chỉ gửi tin nhắn đến chat ID đã cấu hình, không public.

### 3. Rate Limiting

Telegram có giới hạn:
- **30 messages/second** per bot
- **1 message/second** per chat (group/channel)

Service đã được implement với error handling để tránh vượt quá giới hạn.

### 4. Error Handling

Nếu Telegram API fail:
- Log error nhưng **KHÔNG** làm fail operation chính
- Order vẫn được tạo/cập nhật thành công
- Chỉ notification bị mất

---

## 🧪 Testing

### 1. Test Bot Connectivity

```bash
# Check bot info
curl https://api.telegram.org/bot8370737734:AAFKdJE_WqS2G4lVKQxT9jbzI2yfi59JEhQ/getMe

# Response should include bot info
{
  "ok": true,
  "result": {
    "id": 8370737734,
    "is_bot": true,
    "first_name": "Restaurant Kitchen",
    "username": "YourBotUsername"
  }
}
```

### 2. Test Send Message

```bash
# Replace CHAT_ID with your actual chat ID
curl -X POST https://api.telegram.org/bot8370737734:AAFKdJE_WqS2G4lVKQxT9jbzI2yfi59JEhQ/sendMessage \
  -H "Content-Type: application/json" \
  -d '{
    "chat_id": "YOUR_CHAT_ID",
    "text": "🧪 Test message from API",
    "parse_mode": "HTML"
  }'
```

### 3. Test from Application

```bash
# Create order to trigger notification
curl -X POST http://localhost:8080/api/orders/table/1/get-or-create \
  -H "Content-Type: application/json"

# Check logs for Telegram notification
tail -f backend-local.log | grep -i telegram
```

---

## 📊 Monitoring & Logs

### Application Logs

```bash
# Tất cả Telegram activities
tail -f backend-local.log | grep "Telegram"

# Chỉ errors
tail -f backend-local.log | grep "Failed to send Telegram"

# Success messages
tail -f backend-local.log | grep "Telegram message sent successfully"
```

### Log Messages

```
✅ Success:
INFO: Telegram message sent successfully to chat ID: 123456789

❌ Error:
ERROR: Failed to send Telegram message: Connection timeout

ℹ️ Disabled:
INFO: Telegram bot is disabled or chat ID not configured
```

---

## 🚨 Troubleshooting

### Bot không gửi tin nhắn

**1. Check TELEGRAM_BOT_ENABLED:**
```bash
echo $TELEGRAM_BOT_ENABLED
# Should be: true
```

**2. Check TELEGRAM_BOT_CHAT_ID:**
```bash
echo $TELEGRAM_BOT_CHAT_ID
# Should not be empty
```

**3. Verify bot token:**
```bash
curl https://api.telegram.org/bot8370737734:AAFKdJE_WqS2G4lVKQxT9jbzI2yfi59JEhQ/getMe
```

**4. Check if bot was blocked:**
- Bot có thể bị user/group block
- Kick và thêm lại bot vào group

**5. Restart application:**
```bash
# Stop backend
pkill -f spring-boot

# Start again
cd BackEnd
./mvnw spring-boot:run
```

### Chat ID không đúng

```
ERROR: Bad Request: chat not found
```

**Solution:** Verify Chat ID bằng cách gửi /start cho bot.

### Token không hợp lệ

```
ERROR: Unauthorized
```

**Solution:** Check lại token, có thể token bị revoke hoặc bot bị xóa.

---

## 📖 API Endpoints (New Order Flow)

### 1. Get or Create Active Order

```http
POST /api/orders/table/{tableId}/get-or-create?customerId={customerId}
```

**Response:**
```json
{
  "message": "Active order retrieved/created successfully",
  "order": {
    "id": 123,
    "status": "ACTIVE",
    "tableId": 5,
    "customerId": 10,
    "totalAmount": 0,
    "orderItems": []
  }
}
```

### 2. Add Items to Active Order

```http
POST /api/orders/table/{tableId}/add-items?customerId={customerId}
Content-Type: application/json

[
  {
    "menuItemId": 1,
    "quantity": 2,
    "notes": "Ít đường"
  }
]
```

**Response:**
```json
{
  "message": "Items added successfully",
  "order": {
    "id": 123,
    "totalAmount": 150000,
    "orderItems": [...]
  }
}
```

### 3. Close Order

```http
PUT /api/orders/{orderId}/close
```

**Response:**
```json
{
  "message": "Order closed successfully",
  "order": {
    "id": 123,
    "status": "COMPLETED"
  }
}
```

---

## 📝 Quick Setup Checklist

- [ ] Lấy Chat ID từ bot (gửi /start)
- [ ] Set environment variable `TELEGRAM_BOT_CHAT_ID`
- [ ] Set environment variable `TELEGRAM_BOT_ENABLED=true`
- [ ] Restart backend
- [ ] Test tạo order mới
- [ ] Verify nhận được notification trong Telegram
- [ ] Test add items
- [ ] Test checkout

---

## 🎯 Next Steps

1. **Cung cấp Chat ID** để bật notification
2. **Test order flow** với flow mới
3. **Monitor logs** để đảm bảo notification hoạt động
4. **Customize messages** nếu cần (edit `TelegramBotServiceImpl.java`)

---

**Token hiện tại:** `8370737734:AAFKdJE_WqS2G4lVKQxT9jbzI2yfi59JEhQ`

**Cần cung cấp:** Chat ID (sẽ nhận được sau khi gửi /start cho bot)





