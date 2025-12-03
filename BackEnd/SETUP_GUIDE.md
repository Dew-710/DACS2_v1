# 🚀 Restaurant Management System - Setup Guide

## 📋 Yêu cầu hệ thống

- **Java**: JDK 17+
- **Maven**: 3.6+
- **PostgreSQL**: 15+ (hoặc Docker)
- **Node.js**: 18+ (cho ESP32 development)
- **Docker**: (optional, for easy setup)

## 🗄️ Cài đặt Database

### Cách 1: Dùng Docker (Khuyến nghị)

```bash
# 1. Start PostgreSQL và PgAdmin
docker-compose up -d

# 2. Kiểm tra database
docker ps

# 3. Import schema (tự động nếu dùng docker-compose)
# Schema sẽ được import tự động từ database_schema.sql
```

### Cách 2: PostgreSQL Local

```bash
# 1. Cài đặt PostgreSQL
# macOS: brew install postgresql
# Ubuntu: sudo apt install postgresql

# 2. Tạo database
createdb restaurant

# 3. Tạo user
createuser dew_x_phatdev -P
# Password: 123456789

# 4. Import schema
psql -U dew_x_phatdev -d restaurant -f database_schema.sql
```

## ⚙️ Cấu hình Spring Boot

### 1. Copy environment variables
```bash
cp env.example .env
# Edit .env với thông tin database của bạn
```

### 2. Cập nhật application.properties
```properties
# Sử dụng environment variables
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

# Hoặc cấu hình trực tiếp
spring.datasource.url=jdbc:postgresql://localhost:5432/restaurant
spring.datasource.username=dew_x_phatdev
spring.datasource.password=123456789
```

## 🏃‍♂️ Chạy ứng dụng

### Development Mode
```bash
# 1. Clean và compile
mvn clean compile

# 2. Chạy ứng dụng
mvn spring-boot:run

# 3. Kiểm tra
curl http://localhost:8080/api/users/list
```

### Production Mode
```bash
# 1. Build JAR
mvn clean package -DskipTests

# 2. Chạy JAR
java -jar target/BackEnd-0.0.1-SNAPSHOT.jar
```

## 🧪 Test hệ thống

### 1. Kiểm tra Database Connection
```bash
# Test API
curl http://localhost:8080/api/users/list
curl http://localhost:8080/api/tables/list
curl http://localhost:8080/api/menu-items/list
```

### 2. Test Booking System
```bash
# Kiểm tra bàn trống
curl "http://localhost:8080/api/bookings/availability?date=2024-01-01&time=19:00&guests=4"

# Đặt bàn
curl -X POST http://localhost:8080/api/bookings/create \
  -H "Content-Type: application/json" \
  -d '{
    "customer": {"id": 3},
    "table": {"id": 1},
    "date": "2024-01-01",
    "time": "19:00:00",
    "guests": 2
  }'
```

### 3. Test QR Ordering
```bash
# Generate QR cho bàn
curl -X POST http://localhost:8080/api/tables/1/generate-qr

# Check-in qua QR
curl -X POST "http://localhost:8080/api/tables/checkin/TABLE-001?customerId=3"

# Thêm món vào order
curl -X POST http://localhost:8080/api/orders/1/add-items \
  -H "Content-Type: application/json" \
  -d '[{"menuItem": {"id": 1}, "quantity": 2}]'
```

## 🌐 WebSocket Testing

### Kitchen Display
```javascript
// Connect to kitchen WebSocket
const ws = new WebSocket('ws://localhost:8080/ws/kitchen?client=kitchen');

// Listen for messages
ws.onmessage = (event) => {
    console.log('Kitchen notification:', event.data);
    // "KITCHEN:NEW_ORDER|Table 01|Order #123"
};
```

### ESP32 Testing
```javascript
// Connect to ESP32 WebSocket
const ws = new WebSocket('ws://localhost:8080/ws/iot?client=esp32');

// Send ready message
ws.send('ESP32 ready!');

// Listen for image data
ws.onmessage = (event) => {
    console.log('ESP32 data:', event.data);
    // "IMG|1/5|base64data..."
};
```

## 📊 PgAdmin Access

Nếu dùng Docker:
- **URL**: http://localhost:8085
- **Email**: admin@restaurant.com
- **Password**: admin123

## 🔧 Troubleshooting

### Database Connection Issues
```bash
# Kiểm tra PostgreSQL running
docker ps | grep postgres

# Kiểm tra logs
docker logs restaurant-postgres

# Test connection
psql -U dew_x_phatdev -d restaurant -c "SELECT * FROM users;"
```

### Port Conflicts
```bash
# Kiểm tra port 8080
lsof -i :8080

# Kill process nếu cần
kill -9 <PID>

# Hoặc đổi port trong application.properties
server.port=8081
```

### Build Issues
```bash
# Clean Maven cache
mvn clean
rm -rf ~/.m2/repository/com/restaurant

# Rebuild
mvn compile
```

## 📱 Mobile App Development

### React Native Setup (optional)
```bash
# Tạo mobile app
npx react-native init RestaurantMobile

# Install dependencies
npm install socket.io-client axios

# Connect to backend
const API_BASE = 'http://192.168.1.100:8080'; // IP máy của bạn
```

## 🤖 ESP32 Development

### Arduino IDE Setup
```cpp
#include <WebSocketsClient.h>
#include <WiFi.h>

const char* ssid = "YourWiFi";
const char* password = "YourPassword";
const char* ws_host = "192.168.1.100"; // IP máy backend
const uint16_t ws_port = 8080;

WebSocketClient ws;

void setup() {
    Serial.begin(115200);
    WiFi.begin(ssid, password);

    ws.begin(ws_host, ws_port, "/ws/iot?client=esp32");
    ws.onEvent(webSocketEvent);
}

void webSocketEvent(WStype_t type, uint8_t * payload, size_t length) {
    // Handle WebSocket events
}
```

## 📈 Monitoring & Logs

### Application Logs
```bash
# Xem logs real-time
tail -f logs/spring.log

# Debug mode
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"
```

### Database Logs
```bash
# PostgreSQL logs
docker logs restaurant-postgres

# Query performance
psql -U dew_x_phatdev -d restaurant -c "SELECT * FROM pg_stat_activity;"
```

## 🚀 Production Deployment

### 1. Build Production JAR
```bash
mvn clean package -DskipTests -Pproduction
```

### 2. Environment Setup
```bash
# Production .env
DB_HOST=production-db-host
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=your-secure-jwt-secret
```

### 3. Docker Production
```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  app:
    image: restaurant-backend:latest
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      - postgres
```

## 📞 Support

Nếu gặp vấn đề:
1. Check logs: `docker logs restaurant-postgres`
2. Verify database: `psql -U dew_x_phatdev -d restaurant -c "SELECT * FROM users;"`
3. Test API: `curl http://localhost:8080/actuator/health`
4. Check network: `ping localhost`

---

**Chúc mừng! Hệ thống nhà hàng của bạn đã sẵn sàng!** 🎉
