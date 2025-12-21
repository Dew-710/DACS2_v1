# Hướng dẫn sử dụng QR Code với ESP32

## Tổng quan

Hệ thống hỗ trợ tạo và gửi QR code tới ESP32 để hiển thị trên màn hình. Khách hàng có thể quét QR code để truy cập menu và đặt món trực tiếp.

## Luồng hoạt động

1. **Admin/Staff tạo QR code cho bàn** → QR code được lưu vào database
2. **Gửi QR code tới ESP32** → QR code được hiển thị trên màn hình ESP32
3. **Khách hàng quét QR code** → Truy cập menu tại `/menu/[QR_CODE]`
4. **Khách hàng chọn món và đặt hàng** → Đơn hàng được tạo tự động

## API Endpoints

### 1. Generate QR Code cho bàn
```bash
POST /api/tables/{tableId}/generate-qr
```

### 2. Gửi QR Code tới ESP32
```bash
POST /api/send-qr-code/{tableId}
```

### 3. Lấy QR Code Image
```bash
GET /api/qr-code/{tableId}/image
```

### 4. Lấy thông tin bàn từ QR Code
```bash
GET /api/tables/qr/{qrCode}
```

## WebSocket Connection

ESP32 kết nối tới WebSocket endpoint:
```
ws://localhost:8080/ws/iot?clientType=esp32
```

Khi nhận được QR code image, ESP32 sẽ nhận dữ liệu base64 được chia thành các chunks.

## Cấu hình ESP32

### Kết nối WebSocket
```cpp
WebSocketsClient webSocket;

void setup() {
  webSocket.begin("localhost", 8080, "/ws/iot?clientType=esp32");
  webSocket.onEvent(webSocketEvent);
}

void webSocketEvent(WStype_t type, uint8_t * payload, size_t length) {
  switch(type) {
    case WStype_DISCONNECTED:
      Serial.println("WebSocket disconnected");
      break;
    case WStype_CONNECTED:
      Serial.println("WebSocket connected");
      break;
    case WStype_TEXT:
      // Nhận base64 chunks và decode thành image
      handleQRCodeChunk((char*)payload);
      break;
  }
}
```

### Xử lý QR Code Image
ESP32 nhận QR code image dưới dạng base64 chunks, cần:
1. Ghép các chunks lại thành base64 string hoàn chỉnh
2. Decode base64 thành JPEG/PNG image
3. Hiển thị trên màn hình LCD

## Frontend Usage

### Staff Dashboard - QR Codes Tab
- Xem tất cả QR codes của các bàn
- Click "Gửi tới ESP32" để gửi QR code tới ESP32
- Copy QR code để in hoặc sử dụng

### Customer Menu Page
- URL: `http://localhost:3000/menu/[QR_CODE]`
- Khách hàng quét QR code → Truy cập menu
- Chọn món và đặt hàng trực tiếp

## Environment Variables

Để cấu hình URL frontend cho QR code:
```bash
export FRONTEND_URL=http://your-frontend-url.com
```

Nếu không set, mặc định sẽ dùng `http://localhost:3000`.

## Testing

### Test QR Code Generation
```bash
curl -X POST http://localhost:8080/api/tables/1/generate-qr
```

### Test Send to ESP32
```bash
curl -X POST http://localhost:8080/api/send-qr-code/1
```

### Test QR Code Image
```bash
curl http://localhost:8080/api/qr-code/1/image -o qr.png
```

## Notes

- QR code chứa URL: `{FRONTEND_URL}/menu/{QR_CODE}`
- ESP32 cần kết nối WebSocket để nhận QR code images
- QR code images được resize về 128x128 pixels cho ESP32 display
- Format: JPEG với compression quality 0.85

