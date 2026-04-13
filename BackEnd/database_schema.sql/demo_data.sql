-- ============================================================
--  Restaurant Management System - Dữ liệu Demo sát thực tế
--  Luồng: Đặt bàn -> Check-in -> Gọi món -> Lịch sử
-- ============================================================

-- 1. NGƯỜI DÙNG (Mật khẩu mặc định: 'password123')
INSERT INTO users (username, password_hash, full_name, phone, email, role, status, created_at, updated_at)
VALUES
    ('staff_dung', '$2a$10$8.VAg5iK6uK1I/3M1eYpP.9Z0r5vYn66t.Jz6Z.r6Y6r6Y6r6Y6r6', 'NV Nguyễn Dũng', '0911111111', 'dung.staff@restaurant.com', 'STAFF', 'ACTIVE', NOW(), NOW()),
    ('customer_le', '$2a$10$8.VAg5iK6uK1I/3M1eYpP.9Z0r5vYn66t.Jz6Z.r6Y6r6Y6r6Y6r6', 'Khách Lê Hoàng', '0922222222', 'lehoanhdung710@gmail.com', 'CUSTOMER', 'ACTIVE', NOW(), NOW())
ON CONFLICT (username) DO NOTHING;

-- 2. THỰC ĐƠN (Để khách xem menu)
INSERT INTO categories (name, description, display_order, is_active, created_at, updated_at)
VALUES
    ('Món Khai Vị', 'Bắt đầu bữa ăn nhẹ nhàng', 1, TRUE, NOW(), NOW()),
    ('Món Chính', 'Tinh hoa ẩm thực nhà hàng', 2, TRUE, NOW(), NOW()),
    ('Đồ Uống', 'Giải khát và trà', 3, TRUE, NOW(), NOW());

INSERT INTO menu_items (name, price, description, category_id, is_available, created_at, updated_at)
VALUES
    ('Súp Bào Ngư', 120000, 'Súp nóng hổi bồi bổ sức khỏe', 1, TRUE, NOW(), NOW()),
    ('Gỏi Xoài Tôm Khô', 65000, 'Vị chua ngọt kích thích vị giác', 1, TRUE, NOW(), NOW()),
    ('Bò Lúc Lắc', 155000, 'Thịt bò mềm kèm khoai tây chiên', 2, TRUE, NOW(), NOW()),
    ('Cơm Chiên Hải Sản', 95000, 'Cơm chiên giòn cùng mực và tôm', 2, TRUE, NOW(), NOW()),
    ('Trà Đào Cam Sả', 35000, 'Thức uống thanh mát cho mùa hè', 3, TRUE, NOW(), NOW()),
    ('Nước Suối', 10000, 'Nước tinh khiết', 3, TRUE, NOW(), NOW());

-- 3. DANH SÁCH BÀN (Để Staff quản lý trạng thái)
INSERT INTO tables (table_name, capacity, status, table_type, location, qr_code, created_at, updated_at)
VALUES
    ('Bàn 01', 4, 'VACANT', 'INDOOR', 'Khu A', 'QR-01', NOW(), NOW()),
    ('Bàn 02', 2, 'OCCUPIED', 'WINDOW', 'Khu B', 'QR-02', NOW(), NOW()), -- Bàn đang có khách ăn
    ('Bàn 03', 6, 'RESERVED', 'VIP', 'Phòng 102', 'QR-03', NOW(), NOW()); -- Bàn đã được đặt trước

-- 4. LỊCH SỬ ĐẶT BÀN (Dành cho khách xem History)
INSERT INTO bookings (customer_id, table_id, booking_date, booking_time, guests, status, booking_code, created_at)
VALUES
    -- Lịch sử quá khứ
    ((SELECT id FROM users WHERE username = 'customer_le'), (SELECT id FROM tables WHERE table_name = 'Bàn 01'), CURRENT_DATE - INTERVAL '5 days', '18:30:00', 4, 'COMPLETED', 'BK001', NOW() - INTERVAL '5 days'),
    -- Lịch đặt hôm nay (Staff sẽ check cái này)
    ((SELECT id FROM users WHERE username = 'customer_le'), (SELECT id FROM tables WHERE table_name = 'Bàn 03'), CURRENT_DATE, '19:00:00', 6, 'CONFIRMED', 'BK002', NOW());

-- 5. LỊCH SỬ ĂN UỐNG (Dành cho khách xem History các món đã ăn)
-- Đơn hàng đã hoàn thành cách đây 5 ngày
WITH past_order AS (
    INSERT INTO orders (customer_id, staff_id, table_id, booking_id, order_time, status, total_amount, payment_status, created_at)
    VALUES (
        (SELECT id FROM users WHERE username = 'customer_le'),
        (SELECT id FROM users WHERE username = 'staff_dung'),
        (SELECT id FROM tables WHERE table_name = 'Bàn 01'),
        (SELECT id FROM bookings WHERE booking_code = 'BK001'),
        NOW() - INTERVAL '5 days', 'SERVED', 255000, 'PAID', NOW() - INTERVAL '5 days'
    ) RETURNING id
)
INSERT INTO order_items (order_id, menu_item_id, quantity, price, status, is_confirmed, created_at)
SELECT id, (SELECT id FROM menu_items WHERE name = 'Bò Lúc Lắc'), 1, 155000, 'SERVED', TRUE, NOW() - INTERVAL '5 days' FROM past_order
UNION ALL
SELECT id, (SELECT id FROM menu_items WHERE name = 'Cơm Chiên Hải Sản'), 1, 95000, 'SERVED', TRUE, NOW() - INTERVAL '5 days' FROM past_order
UNION ALL
SELECT id, (SELECT id FROM menu_items WHERE name = 'Nước Suối'), 1, 5000, 'SERVED', TRUE, NOW() - INTERVAL '5 days' FROM past_order;

-- 6. ĐƠN HÀNG HIỆN TẠI (Bàn 02 đang ăn - Staff có thể gửi QR thanh toán)
WITH current_order AS (
    INSERT INTO orders (customer_id, staff_id, table_id, order_time, status, total_amount, payment_status, qr_code, created_at)
    VALUES (
        (SELECT id FROM users WHERE username = 'customer_le'),
        (SELECT id FROM users WHERE username = 'staff_dung'),
        (SELECT id FROM tables WHERE table_name = 'Bàn 02'),
        NOW(), 'PREPARING', 190000, 'PENDING', 'https://api.payos.vn/v2/payment-requests/example', NOW()
    ) RETURNING id
)
INSERT INTO order_items (order_id, menu_item_id, quantity, price, status, is_confirmed, created_at)
SELECT id, (SELECT id FROM menu_items WHERE name = 'Súp Bào Ngư'), 1, 120000, 'PREPARING', TRUE, NOW() FROM current_order
UNION ALL
SELECT id, (SELECT id FROM menu_items WHERE name = 'Trà Đào Cam Sả'), 2, 35000, 'READY', TRUE, NOW() FROM current_order;
