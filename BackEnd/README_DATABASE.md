# 🏪 Restaurant Management System - Database Setup

## 📋 Tổng quan Database

Database PostgreSQL cho hệ thống quản lý nhà hàng với các tính năng:
- Đặt bàn trực tuyến
- Quét QR để order
- Quản lý bếp và nhân viên
- Thanh toán tích hợp ESP32

## 🗄️ Cấu trúc Database

### Core Tables:
- `users` - Khách hàng, nhân viên, admin
- `tables` - Thông tin bàn ăn với QR code
- `bookings` - Đặt bàn trước
- `orders` - Đơn hàng tại bàn
- `order_items` - Chi tiết món trong order
- `menu_items` - Menu món ăn
- `categories` - Danh mục món
- `payments` - Thanh toán

## 🚀 Cách cài đặt

### 1. Tạo Database PostgreSQL

```bash
# Nếu dùng Docker
docker run --name restaurant-postgres \
  -e POSTGRES_DB=restaurant \
  -e POSTGRES_USER=dew_x_phatdev \
  -e POSTGRES_PASSWORD=123456789 \
  -p 5432:5432 \
  -d postgres:15

# Hoặc tạo database thủ công
createdb -U postgres restaurant
```

### 2. Chạy Schema

```bash
# Import schema
psql -U dew_x_phatdev -d restaurant -f database_schema.sql

# Hoặc dùng Docker
docker exec -i restaurant-postgres psql -U dew_x_phatdev -d restaurant < database_schema.sql
```

### 3. Kiểm tra cài đặt

```bash
# Kết nối database
psql -U dew_x_phatdev -d restaurant

# Xem tables
\dt

# Xem sample data
SELECT * FROM users;
SELECT * FROM tables;
SELECT * FROM menu_items;
```

## 📊 Sample Data

### Users:
- Admin: `admin` / password
- Staff: `staff1` / password
- Customers: `customer1`, `customer2` / password

### Tables với QR codes:
- Table 01: `TABLE-001`
- Table 02: `TABLE-002`
- etc.

### Menu Items:
- Appetizers, Main Courses, Desserts, Beverages, Specials

## 🔍 Useful Queries

### Kiểm tra bàn trống cho ngày cụ thể
```sql
SELECT t.table_name, t.capacity, t.table_type
FROM tables t
WHERE t.status = 'VACANT'
  AND NOT EXISTS (
    SELECT 1 FROM bookings b
    WHERE b.table_id = t.id
      AND b.booking_date = '2024-01-01'
      AND b.booking_time = '19:00:00'
      AND b.status != 'CANCELLED'
  );
```

### Báo cáo doanh thu hàng ngày
```sql
SELECT
    DATE(o.order_time) as sale_date,
    COUNT(*) as total_orders,
    SUM(o.total_amount) as total_revenue,
    AVG(o.total_amount) as avg_order_value
FROM orders o
WHERE o.status = 'PAID'
GROUP BY DATE(o.order_time);
```

### Món ăn bán chạy nhất
```sql
SELECT
    mi.name,
    SUM(oi.quantity) as total_sold,
    SUM(oi.subtotal) as total_revenue
FROM menu_items mi
JOIN order_items oi ON mi.id = oi.menu_item_id
JOIN orders o ON oi.order_id = o.id
WHERE o.status = 'PAID'
GROUP BY mi.id, mi.name
ORDER BY total_sold DESC;
```

## 🔧 Database Configuration

Trong `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/restaurant
spring.datasource.username=dew_x_phatdev
spring.datasource.password=123456789
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

## 📈 Views & Reports

### Daily Sales Report
```sql
SELECT * FROM daily_sales_report;
```

### Table Utilization
```sql
SELECT * FROM table_utilization;
```

### Popular Menu Items
```sql
SELECT * FROM popular_menu_items;
```

## 🔒 Security & Permissions

- `restaurant_admin`: Full access
- `restaurant_staff`: CRUD operations
- `restaurant_customer`: Read-only access to menu, tables

## 🧪 Testing

### Test Booking System
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
    "guests": 2,
    "note": "Birthday dinner"
  }'
```

### Test QR Ordering
```bash
# Check-in qua QR
curl -X POST "http://localhost:8080/api/tables/checkin/TABLE-001?customerId=3"

# Thêm món vào order
curl -X POST http://localhost:8080/api/orders/1/add-items \
  -H "Content-Type: application/json" \
  -d '[{"menuItem": {"id": 1}, "quantity": 2}]'
```

## 🔄 Migration Scripts

Khi cần update schema:
1. Tạo file migration mới: `V2__add_new_feature.sql`
2. Chạy migration
3. Update entity classes trong Spring Boot

## 📞 Support

Nếu gặp vấn đề với database:
1. Kiểm tra PostgreSQL logs
2. Verify connection string
3. Check user permissions
4. Test với sample queries

---

**Database này được tối ưu cho hệ thống nhà hàng thực tế với workflow hoàn chỉnh!** 🎉
