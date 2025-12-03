# 🐘 PostgreSQL Database Setup

## 🚀 Quick Start

### 1. Start Database
```bash
./start-db.sh
```

### 2. Start Spring Boot Application
```bash
mvn spring-boot:run
```

### 3. Stop Database
```bash
./stop-db.sh
```

## 📊 Database Info

- **Database**: restaurant
- **Username**: dew_x_phatdev
- **Password**: 123456789
- **Port**: 5432

## 🌐 Web Interfaces

- **PgAdmin**: http://localhost:8085
  - Email: admin@restaurant.com
  - Password: admin123

- **H2 Console** (fallback): http://localhost:8080/h2-console

## 🧪 Test APIs

```bash
# Test database connection
curl http://localhost:8080/api/users/list

# Test tables
curl http://localhost:8080/api/tables/all

# Test bookings
curl "http://localhost:8080/api/bookings/availability?date=2024-01-01&time=19:00&guests=4"
```

## 📋 Tables Created

- `users` - Khách hàng, nhân viên
- `tables` - Bàn ăn với QR codes
- `bookings` - Đặt bàn trước
- `orders` - Đơn hàng tại bàn
- `order_items` - Chi tiết món ăn
- `menu_items` - Menu
- `categories` - Danh mục món
- `payments` - Thanh toán

## 🔧 Manual Database Access

```bash
# Connect to PostgreSQL
docker exec -it restaurant-postgres psql -U dew_x_phatdev -d restaurant

# View tables
\dt

# View sample data
SELECT * FROM users;
SELECT * FROM tables;
```

## 📖 Detailed Documentation

- `DOCKER_SETUP.md` - Chi tiết setup Docker
- `database_schema.sql` - Database schema
- `README_DATABASE.md` - Database documentation

---

**PostgreSQL với Docker đã sẵn sàng!** 🎉
