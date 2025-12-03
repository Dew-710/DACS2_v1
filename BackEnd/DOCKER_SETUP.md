# 🐳 Restaurant Management System - Docker PostgreSQL Setup

## 📋 Yêu cầu

- **Docker**: 20.10+
- **Docker Compose**: 2.0+

## 🚀 Khởi động PostgreSQL với Docker

### 1. Start Database
```bash
# Từ thư mục gốc của project
docker-compose up -d

# Hoặc chỉ start PostgreSQL
docker-compose up -d postgres
```

### 2. Kiểm tra containers
```bash
docker ps

# Bạn sẽ thấy:
# restaurant-postgres (PostgreSQL)
# restaurant-pgadmin (PgAdmin web interface)
```

### 3. Kiểm tra logs
```bash
# Xem logs PostgreSQL
docker logs restaurant-postgres

# Xem logs PgAdmin
docker logs restaurant-pgadmin
```

## 🔧 Truy cập Database

### PgAdmin Web Interface
- **URL**: http://localhost:8085
- **Email**: admin@restaurant.com
- **Password**: admin123

### Kết nối trực tiếp
```bash
# Kết nối vào container PostgreSQL
docker exec -it restaurant-postgres psql -U dew_x_phatdev -d restaurant

# Hoặc từ máy host
psql -h localhost -p 5432 -U dew_x_phatdev -d restaurant
```

## 📊 Database Information

- **Database**: restaurant
- **Username**: dew_x_phatdev
- **Password**: 123456789
- **Port**: 5432
- **Schema**: Tự động import từ `database_schema.sql`

## 🧪 Test Database Connection

### 1. Kiểm tra tables
```sql
-- Trong psql hoặc PgAdmin
\dt

-- Bạn sẽ thấy các tables:
-- bookings, categories, menu_items, orders, order_items, payments, tables, users
```

### 2. Kiểm tra sample data
```sql
SELECT * FROM users;
SELECT * FROM tables;
SELECT * FROM menu_items LIMIT 5;
```

## 🔄 Import Schema Thủ Công (nếu cần)

Nếu schema không tự động import:

```bash
# Copy file vào container
docker cp database_schema.sql restaurant-postgres:/tmp/

# Import vào database
docker exec -it restaurant-postgres psql -U dew_x_phatdev -d restaurant -f /tmp/database_schema.sql
```

## 🏃‍♂️ Chạy Spring Boot Application

### 1. Sau khi database đã sẵn sàng
```bash
# Trong terminal riêng
cd /path/to/BackEnd
mvn spring-boot:run
```

### 2. Test APIs
```bash
# Test connection
curl http://localhost:8080/api/users/list

# Test database
curl http://localhost:8080/api/tables/all
curl http://localhost:8080/api/menu-items/list
```

## 📊 Monitoring Database

### PgAdmin Features
- **Query Tool**: Chạy SQL queries
- **Table Data**: Xem và edit data
- **ERD**: Xem database schema
- **Backup/Restore**: Sao lưu database

### Useful Queries
```sql
-- Kiểm tra kết nối active
SELECT * FROM pg_stat_activity;

-- Kiểm tra size database
SELECT pg_size_pretty(pg_database_size('restaurant'));

-- Xem recent queries (nếu log được bật)
SELECT * FROM pg_stat_statements LIMIT 10;
```

## 🔧 Troubleshooting

### PostgreSQL không start
```bash
# Stop và restart
docker-compose down
docker-compose up -d postgres

# Check logs
docker logs restaurant-postgres
```

### Port conflicts
```bash
# Nếu port 5432 bị chiếm
docker-compose down
docker-compose up -d --scale postgres=0
# Thay đổi port trong docker-compose.yml
```

### Schema import failed
```bash
# Import thủ công
docker exec -it restaurant-postgres psql -U dew_x_phatdev -d restaurant < database_schema.sql
```

### Application không kết nối được database
```bash
# Kiểm tra network
docker network ls
docker network inspect restaurant-network

# Test connection từ container
docker exec restaurant-postgres pg_isready -h localhost -p 5432 -U dew_x_phatdev -d restaurant
```

## 🗂️ Database Backup & Restore

### Backup
```bash
# Backup database
docker exec restaurant-postgres pg_dump -U dew_x_phatdev restaurant > backup.sql

# Backup với custom format
docker exec restaurant-postgres pg_dump -U dew_x_phatdev -Fc restaurant > backup.dump
```

### Restore
```bash
# Restore từ SQL file
docker exec -i restaurant-postgres psql -U dew_x_phatdev -d restaurant < backup.sql

# Restore từ custom format
docker exec restaurant-postgres pg_restore -U dew_x_phatdev -d restaurant backup.dump
```

## 🛑 Stop & Cleanup

### Stop services
```bash
# Stop tất cả
docker-compose down

# Stop PostgreSQL only
docker-compose stop postgres
```

### Cleanup (xóa data)
```bash
# Xóa containers và volumes
docker-compose down -v

# Hoặc xóa volume cụ thể
docker volume rm backend_postgres_data
```

## 📋 Environment Variables

Nếu cần custom config, tạo file `.env`:

```env
POSTGRES_DB=restaurant
POSTGRES_USER=dew_x_phatdev
POSTGRES_PASSWORD=123456789
PGADMIN_EMAIL=admin@restaurant.com
PGADMIN_PASSWORD=admin123
```

---

**Database PostgreSQL với Docker đã sẵn sàng!** 🚀

Chạy `docker-compose up -d` và bắt đầu phát triển ứng dụng của bạn!
