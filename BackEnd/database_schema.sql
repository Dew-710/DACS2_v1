-- =============================================
-- RESTAURANT MANAGEMENT SYSTEM DATABASE SCHEMA
-- PostgreSQL Database Creation Script
-- =============================================

-- Create database (run this separately if needed)
-- CREATE DATABASE restaurant_db;
-- \c restaurant_db;

-- =============================================
-- 1. USERS TABLE
-- =============================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER', 'STAFF', 'ADMIN')),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for users table
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);

-- =============================================
-- 2. CATEGORIES TABLE (for menu items)
-- =============================================
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    image_url VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for categories table
CREATE INDEX idx_categories_active ON categories(is_active);
CREATE INDEX idx_categories_order ON categories(display_order);

-- =============================================
-- 3. MENU ITEMS TABLE
-- =============================================
CREATE TABLE menu_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    image_url VARCHAR(255),
    category_id BIGINT REFERENCES categories(id),
    is_available BOOLEAN DEFAULT TRUE,
    preparation_time INTEGER DEFAULT 15, -- minutes
    calories INTEGER,
    allergens TEXT[], -- array of allergens
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for menu_items table
CREATE INDEX idx_menu_items_category ON menu_items(category_id);
CREATE INDEX idx_menu_items_available ON menu_items(is_available);
CREATE INDEX idx_menu_items_price ON menu_items(price);

-- =============================================
-- 4. RESTAURANT TABLES TABLE
-- =============================================
CREATE TABLE tables (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL UNIQUE,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    status VARCHAR(20) DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'RESERVED', 'OCCUPIED', 'CLEANING', 'MAINTENANCE')),
    qr_code VARCHAR(100) UNIQUE,
    table_type VARCHAR(20) DEFAULT 'STANDARD' CHECK (table_type IN ('STANDARD', 'VIP', 'WINDOW', 'OUTDOOR', 'BAR')),
    location VARCHAR(50), -- e.g., "Floor 1", "Near Window"
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for tables table
CREATE INDEX idx_tables_status ON tables(status);
CREATE INDEX idx_tables_type ON tables(table_type);
CREATE INDEX idx_tables_capacity ON tables(capacity);

-- =============================================
-- 5. BOOKINGS TABLE
-- =============================================
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES users(id),
    table_id BIGINT REFERENCES tables(id),
    booking_date DATE NOT NULL,
    booking_time TIME NOT NULL,
    guests INTEGER NOT NULL CHECK (guests > 0),
    note TEXT,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    booking_code VARCHAR(20) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Ensure booking date is not in the past
    CONSTRAINT chk_booking_date_future CHECK (booking_date >= CURRENT_DATE)
);

-- Indexes for bookings table
CREATE INDEX idx_bookings_customer ON bookings(customer_id);
CREATE INDEX idx_bookings_table ON bookings(table_id);
CREATE INDEX idx_bookings_date ON bookings(booking_date);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_code ON bookings(booking_code);

-- Unique constraint to prevent double booking same table at same time
CREATE UNIQUE INDEX idx_unique_booking_slot ON bookings(table_id, booking_date, booking_time)
WHERE status NOT IN ('CANCELLED');

-- =============================================
-- 6. ORDERS TABLE
-- =============================================
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES users(id),
    staff_id BIGINT REFERENCES users(id), -- staff who took the order
    table_id BIGINT NOT NULL REFERENCES tables(id),
    booking_id BIGINT REFERENCES bookings(id),
    order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PLACED' CHECK (status IN ('PLACED', 'CONFIRMED', 'PREPARING', 'READY', 'SERVED', 'PAID', 'CANCELLED')),
    total_amount DECIMAL(10,2) DEFAULT 0 CHECK (total_amount >= 0),
    estimated_ready_time TIMESTAMP,
    actual_ready_time TIMESTAMP,
    served_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for orders table
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_staff ON orders(staff_id);
CREATE INDEX idx_orders_table ON orders(table_id);
CREATE INDEX idx_orders_booking ON orders(booking_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_time ON orders(order_time);

-- =============================================
-- 7. ORDER ITEMS TABLE
-- =============================================
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id BIGINT NOT NULL REFERENCES menu_items(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    subtotal DECIMAL(10,2) GENERATED ALWAYS AS (quantity * price) STORED,
    notes TEXT,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PREPARING', 'READY', 'SERVED', 'CANCELLED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for order_items table
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_menu_item ON order_items(menu_item_id);
CREATE INDEX idx_order_items_status ON order_items(status);

-- =============================================
-- 8. PAYMENTS TABLE
-- =============================================
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    amount DECIMAL(10,2) NOT NULL CHECK (amount > 0),
    method VARCHAR(20) NOT NULL CHECK (method IN ('CASH', 'CARD', 'DIGITAL_WALLET', 'BANK_TRANSFER')),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    transaction_id VARCHAR(100) UNIQUE,
    paid_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for payments table
CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_method ON payments(method);
CREATE INDEX idx_payments_transaction ON payments(transaction_id);

-- =============================================
-- TRIGGERS FOR UPDATED_AT
-- =============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers for updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON categories FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_menu_items_updated_at BEFORE UPDATE ON menu_items FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_tables_updated_at BEFORE UPDATE ON tables FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_bookings_updated_at BEFORE UPDATE ON bookings FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_order_items_updated_at BEFORE UPDATE ON order_items FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- SAMPLE DATA INSERTION
-- =============================================

-- Insert sample users
INSERT INTO users (username, email, password_hash, full_name, phone, role, status) VALUES
('admin', 'admin@restaurant.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Administrator', '0123456789', 'ADMIN', 'ACTIVE'),
('staff1', 'staff1@restaurant.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John Staff', '0987654321', 'STAFF', 'ACTIVE'),
('customer1', 'customer1@email.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Alice Customer', '0111111111', 'CUSTOMER', 'ACTIVE'),
('customer2', 'customer2@email.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Bob Customer', '0222222222', 'CUSTOMER', 'ACTIVE');

-- Insert sample categories
INSERT INTO categories (name, description, display_order) VALUES
('Appetizers', 'Starters and small plates', 1),
('Main Courses', 'Main dishes', 2),
('Desserts', 'Sweet treats', 3),
('Beverages', 'Drinks and refreshments', 4),
('Specials', 'Chef special dishes', 5);

-- Insert sample menu items
INSERT INTO menu_items (name, description, price, category_id, preparation_time, is_available) VALUES
('Caesar Salad', 'Fresh romaine lettuce with caesar dressing', 12.99, 1, 5, true),
('Grilled Salmon', 'Fresh Atlantic salmon with herbs', 24.99, 2, 15, true),
('Ribeye Steak', 'Premium beef ribeye steak', 32.99, 2, 20, true),
('Chocolate Cake', 'Rich chocolate cake with vanilla ice cream', 8.99, 3, 5, true),
('Coffee', 'Fresh brewed coffee', 3.99, 4, 2, true),
('Chef Special Pasta', 'House special pasta with seasonal ingredients', 18.99, 5, 12, true);

-- Insert sample tables
INSERT INTO tables (table_name, capacity, status, table_type, location) VALUES
('Table 01', 2, 'VACANT', 'WINDOW', 'Floor 1 - Window Side'),
('Table 02', 4, 'VACANT', 'STANDARD', 'Floor 1 - Center'),
('Table 03', 6, 'VACANT', 'VIP', 'Floor 1 - Private Area'),
('Table 04', 2, 'VACANT', 'BAR', 'Bar Area'),
('Table 05', 8, 'VACANT', 'OUTDOOR', 'Garden Terrace');

-- Generate QR codes for tables (you can run this after inserting tables)
UPDATE tables SET qr_code = 'TABLE-' || LPAD(id::text, 3, '0') WHERE qr_code IS NULL;

-- Insert sample booking
INSERT INTO bookings (customer_id, table_id, booking_date, booking_time, guests, note, status, booking_code) VALUES
(3, 1, CURRENT_DATE + INTERVAL '1 day', '19:00:00', 2, 'Birthday celebration', 'CONFIRMED', 'BK001');

-- =============================================
-- USEFUL QUERIES FOR REPORTING
-- =============================================

-- Daily sales report
CREATE OR REPLACE VIEW daily_sales_report AS
SELECT
    DATE(o.order_time) as sale_date,
    COUNT(DISTINCT o.id) as total_orders,
    COUNT(oi.id) as total_items_sold,
    SUM(o.total_amount) as total_revenue,
    AVG(o.total_amount) as avg_order_value
FROM orders o
JOIN order_items oi ON o.id = oi.order_id
WHERE o.status = 'PAID'
GROUP BY DATE(o.order_time);

-- Table utilization report
CREATE OR REPLACE VIEW table_utilization AS
SELECT
    t.table_name,
    t.capacity,
    COUNT(b.id) as total_bookings,
    COUNT(o.id) as total_orders,
    ROUND(
        EXTRACT(EPOCH FROM (SUM(o.served_time - o.order_time))) / 3600,
        2
    ) as avg_dining_hours
FROM tables t
LEFT JOIN bookings b ON t.id = b.table_id AND b.status = 'COMPLETED'
LEFT JOIN orders o ON t.id = o.table_id AND o.status = 'PAID'
GROUP BY t.id, t.table_name, t.capacity;

-- Popular menu items
CREATE OR REPLACE VIEW popular_menu_items AS
SELECT
    mi.name,
    mi.price,
    c.name as category,
    SUM(oi.quantity) as total_quantity_sold,
    SUM(oi.subtotal) as total_revenue,
    COUNT(DISTINCT oi.order_id) as orders_containing_item
FROM menu_items mi
JOIN categories c ON mi.category_id = c.id
JOIN order_items oi ON mi.id = oi.menu_item_id
JOIN orders o ON oi.order_id = o.id
WHERE o.status = 'PAID'
GROUP BY mi.id, mi.name, mi.price, c.name
ORDER BY total_quantity_sold DESC;

-- =============================================
-- PERMISSIONS AND SECURITY
-- =============================================

-- Create roles
CREATE ROLE restaurant_admin;
CREATE ROLE restaurant_staff;
CREATE ROLE restaurant_customer;

-- Grant permissions (adjust as needed)
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public TO restaurant_staff;
GRANT SELECT ON menu_items, categories, tables TO restaurant_customer;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO restaurant_admin;

-- =============================================
-- END OF SCHEMA
-- =============================================

-- To run this script:
-- psql -U postgres -d restaurant_db -f database_schema.sql

-- Or if using Docker:
-- docker exec -i restaurant-postgres psql -U dew_x_phatdev -d restaurant < database_schema.sql
