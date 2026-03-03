# Quick Start: Telegram Bot & New Order Flow

## ⚡ Bật Telegram Bot trong 3 bước

### Bước 1: Lấy Chat ID (2 phút)

```bash
# Mở Telegram, tìm bot và gửi tin nhắn:
/start

# Bot sẽ trả lời:
✅ Bot đã kích hoạt!
📱 Chat ID của bạn: 123456789

# Copy số Chat ID này
```

### Bước 2: Cấu hình (1 phút)

```bash
# Set environment variable
export TELEGRAM_BOT_ENABLED=true
export TELEGRAM_BOT_CHAT_ID=123456789  # Thay bằng Chat ID của bạn
```

### Bước 3: Restart Backend (30 giây)

```bash
cd BackEnd
./mvnw spring-boot:run
```

**XONG!** 🎉 Bot sẽ bắt đầu gửi thông báo.

---

## 🧪 Test ngay

```bash
# Test tạo order
curl -X POST http://localhost:8080/api/orders/table/1/get-or-create

# Kiểm tra Telegram → Bạn sẽ thấy notification "🔔 ORDER MỚI"
```

---

## 📋 Cơ chế Order Mới

### Trước đây:
```
Khách gọi món lần 1 → Order #1
Khách gọi món lần 2 → Order #2
Khách gọi món lần 3 → Order #3
→ 3 orders riêng biệt cho 1 bàn ❌
```

### Bây giờ:
```
Khách gọi món lần 1 → Order #1
Khách gọi món lần 2 → Thêm vào Order #1
Khách gọi món lần 3 → Thêm vào Order #1
Staff checkout        → Đóng Order #1
→ 1 order duy nhất cho 1 session ✅
```

---

## 🔧 API Endpoints

### 1. Get/Create Active Order
```bash
POST /api/orders/table/{tableId}/get-or-create?customerId={customerId}
```

### 2. Add Items to Order
```bash
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

### 3. Close Order (Checkout)
```bash
PUT /api/orders/{orderId}/close
```

---

## 📱 Telegram Notifications

Mỗi action sẽ tự động gửi notification:

✅ Tạo order mới → "🔔 ORDER MỚI"  
✅ Thêm món → "➕ THÊM MÓN MỚI"  
✅ Đổi trạng thái → "🔄 CẬP NHẬT TRẠNG THÁI"  
✅ Thanh toán → "✅ THANH TOÁN"

---

## 🎯 Frontend Usage

### JavaScript/TypeScript:

```typescript
import { getOrCreateActiveOrder, addItemsToTableOrder, closeOrder } from '@/lib/api';

// 1. Khách check-in hoặc load trang order
const { order } = await getOrCreateActiveOrder(tableId, customerId);

// 2. Khách gọi món
const items = [
  { menuItemId: 1, quantity: 2, notes: "" },
  { menuItemId: 5, quantity: 1, notes: "Ít đường" }
];
const { order: updatedOrder } = await addItemsToTableOrder(tableId, items, customerId);

// 3. Staff checkout
const { order: closedOrder } = await closeOrder(orderId);
```

---

## 🔍 Troubleshooting

### Bot không gửi tin nhắn?

```bash
# Check 1: ENABLED?
echo $TELEGRAM_BOT_ENABLED
# → Phải là "true"

# Check 2: Chat ID set?
echo $TELEGRAM_BOT_CHAT_ID
# → Phải có giá trị (không empty)

# Check 3: Test bot token
curl https://api.telegram.org/bot8370737734:AAFKdJE_WqS2G4lVKQxT9jbzI2yfi59JEhQ/getMe
# → Phải trả về thông tin bot

# Check 4: Xem logs
tail -f BackEnd/backend-local.log | grep -i telegram
```

### Chat ID không đúng?

```bash
# Cách 1: Gửi /start cho bot trong Telegram
# Cách 2: Call API
curl https://api.telegram.org/bot8370737734:AAFKdJE_WqS2G4lVKQxT9jbzI2yfi59JEhQ/getUpdates
# Tìm "chat":{"id":123456789}
```

---

## 📚 Chi tiết thêm

- **Setup đầy đủ:** `TELEGRAM_BOT_SETUP_GUIDE.md`
- **Technical details:** `NEW_ORDER_FLOW_SUMMARY.md`
- **Swagger UI:** http://localhost:8080/swagger-ui.html

---

## ✅ Checklist

- [ ] Gửi `/start` cho bot trong Telegram
- [ ] Copy Chat ID
- [ ] Set `TELEGRAM_BOT_ENABLED=true`
- [ ] Set `TELEGRAM_BOT_CHAT_ID=your_chat_id`
- [ ] Restart backend
- [ ] Test tạo order
- [ ] Verify nhận notification

---

**Token bot:** `8370737734:AAFKdJE_WqS2G4lVKQxT9jbzI2yfi59JEhQ`

**Bạn chỉ cần cung cấp Chat ID!** 🚀









