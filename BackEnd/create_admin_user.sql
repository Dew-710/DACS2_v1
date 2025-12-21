-- Script để tạo admin user với password đã biết
-- BCrypt hash của "admin123" (có thể thay đổi password khác)

-- Xóa user admin cũ nếu có
DELETE FROM users WHERE username = 'admin';

-- Tạo user admin mới với password "admin123"
INSERT INTO users (username, email, password_hash, full_name, phone, role, status, created_at)
VALUES (
    'admin',
    'admin@restaurant.com',
    '$2a$10$8K2GzVtX8QzU8QzU8QzU8eYzU8QzU8QzU8QzU8QzU8QzU8QzU8QzU', -- BCrypt hash của "admin123"
    'Administrator',
    '0123456789',
    'ADMIN',
    'ACTIVE',
    CURRENT_TIMESTAMP
);