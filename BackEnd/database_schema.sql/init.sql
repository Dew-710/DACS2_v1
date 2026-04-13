-- ============================================================
--  Restaurant Management System - PostgreSQL Schema
--  Generated from JPA Entities + Flyway Migrations
--  Compatible with: PostgreSQL 15+ / Docker
-- ============================================================

-- ============================================================
-- EXTENSION
-- ============================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- TABLE: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id             BIGSERIAL PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    full_name      VARCHAR(100),
    phone          VARCHAR(255),
    email          VARCHAR(255),
    role           VARCHAR(50)  NOT NULL,           -- ADMIN | STAFF | CUSTOMER
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE', -- ACTIVE | INACTIVE | SUSPENDED
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP
);

COMMENT ON COLUMN users.role   IS 'ADMIN, STAFF, CUSTOMER';
COMMENT ON COLUMN users.status IS 'ACTIVE, INACTIVE, SUSPENDED';

-- ============================================================
-- TABLE: categories
-- ============================================================
CREATE TABLE IF NOT EXISTS categories (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    description   TEXT,
    image_url     VARCHAR(255),
    display_order INTEGER      NOT NULL DEFAULT 0,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP
);

-- ============================================================
-- TABLE: menu_items
-- ============================================================
CREATE TABLE IF NOT EXISTS menu_items (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(100)   NOT NULL,
    price            NUMERIC(19, 2),
    description      TEXT,
    image_url        TEXT,
    category_id      BIGINT,
    is_available     BOOLEAN        NOT NULL DEFAULT TRUE,
    preparation_time INTEGER,                        -- minutes
    calories         INTEGER,
    allergens        TEXT[],
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP,

    CONSTRAINT fk_menu_item_category
        FOREIGN KEY (category_id) REFERENCES categories (id)
);

-- ============================================================
-- TABLE: tables  (restaurant tables)
-- ============================================================
CREATE TABLE IF NOT EXISTS tables (
    id           BIGSERIAL PRIMARY KEY,
    table_name   VARCHAR(50)  NOT NULL,
    capacity     INTEGER      NOT NULL DEFAULT 0,
    status       VARCHAR(20),                        -- VACANT | RESERVED | OCCUPIED | CLEANING
    qr_code      VARCHAR(100) UNIQUE,
    table_type   VARCHAR(20),                        -- WINDOW | INDOOR | OUTDOOR | VIP
    location     VARCHAR(50),
    last_updated TIMESTAMP,
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP
);

COMMENT ON COLUMN tables.status IS 'VACANT, RESERVED, OCCUPIED, CLEANING';
COMMENT ON COLUMN tables.table_type IS 'WINDOW, INDOOR, OUTDOOR, VIP';

-- ============================================================
-- TABLE: bookings
-- ============================================================
CREATE TABLE IF NOT EXISTS bookings (
    id           BIGSERIAL PRIMARY KEY,
    customer_id  BIGINT      NOT NULL,
    table_id     BIGINT,
    booking_date DATE        NOT NULL,
    booking_time TIME        NOT NULL,
    guests       INTEGER     NOT NULL DEFAULT 0,
    note         TEXT,
    status       VARCHAR(50),
    booking_code VARCHAR(20) UNIQUE,
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,

    CONSTRAINT fk_booking_customer
        FOREIGN KEY (customer_id) REFERENCES users (id),
    CONSTRAINT fk_booking_table
        FOREIGN KEY (table_id) REFERENCES tables (id)
);

-- ============================================================
-- TABLE: orders
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    id                  BIGSERIAL PRIMARY KEY,
    customer_id         BIGINT,
    staff_id            BIGINT,
    table_id            BIGINT        NOT NULL,
    booking_id          BIGINT,
    order_time          TIMESTAMP,
    status              VARCHAR(50),
    total_amount        NUMERIC(19, 2),
    estimated_ready_time TIMESTAMP,
    actual_ready_time   TIMESTAMP,
    served_time         TIMESTAMP,
    payment_status      VARCHAR(50),
    checkout_url        TEXT,
    qr_code             TEXT,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,

    CONSTRAINT fk_order_customer
        FOREIGN KEY (customer_id) REFERENCES users (id),
    CONSTRAINT fk_order_staff
        FOREIGN KEY (staff_id) REFERENCES users (id),
    CONSTRAINT fk_order_table
        FOREIGN KEY (table_id) REFERENCES tables (id),
    CONSTRAINT fk_order_booking
        FOREIGN KEY (booking_id) REFERENCES bookings (id),
    CONSTRAINT orders_status_check
        CHECK (status IN (
            'PLACED', 'CONFIRMED', 'PREPARING', 'READY',
            'SERVED', 'PENDING_PAYMENT', 'CANCELLED'
        ))
);

COMMENT ON CONSTRAINT orders_status_check ON orders
    IS 'Valid order statuses including PENDING_PAYMENT for checkout';

-- ============================================================
-- TABLE: order_items
-- ============================================================
CREATE TABLE IF NOT EXISTS order_items (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT         NOT NULL,
    menu_item_id BIGINT         NOT NULL,
    quantity     INTEGER        NOT NULL DEFAULT 0,
    price        NUMERIC(19, 2),
    notes        TEXT,
    status       VARCHAR(50),                        -- PENDING | PREPARING | READY | SERVED | CANCELLED
    round_number INTEGER        NOT NULL DEFAULT 1,  -- which round of ordering (1st, 2nd, ...)
    is_confirmed BOOLEAN        NOT NULL DEFAULT FALSE, -- FALSE = draft, TRUE = confirmed/billed
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,

    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_item_menu_item
        FOREIGN KEY (menu_item_id) REFERENCES menu_items (id)
);

COMMENT ON COLUMN order_items.round_number IS 'Lượt gọi món thứ mấy (1, 2, 3...)';
COMMENT ON COLUMN order_items.is_confirmed IS 'FALSE = đang draft, TRUE = đã confirm và tính tiền';

-- ============================================================
-- TABLE: payments
-- ============================================================
CREATE TABLE IF NOT EXISTS payments (
    id             BIGSERIAL PRIMARY KEY,
    order_id       BIGINT         NOT NULL,
    amount         NUMERIC(19, 2),
    method         VARCHAR(50),                      -- CASH | CARD | DIGITAL_WALLET | PAYOS | SEPAY
    paid_at        TIMESTAMP,
    status         VARCHAR(50),                      -- PENDING | COMPLETED | FAILED | REFUNDED | CANCELLED
    transaction_id VARCHAR(100)   UNIQUE,
    notes          TEXT,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,

    CONSTRAINT fk_payment_order
        FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT payments_method_check
        CHECK (method IN ('CASH', 'CARD', 'DIGITAL_WALLET', 'PAYOS', 'SEPAY')),
    CONSTRAINT payments_status_check
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED'))
);

-- ============================================================
-- TABLE: payment_transactions  (PayOS integration)
-- ============================================================
CREATE TABLE IF NOT EXISTS payment_transactions (
    id                 BIGSERIAL PRIMARY KEY,
    payos_payment_id   VARCHAR(200) UNIQUE,
    internal_reference VARCHAR(200) UNIQUE NOT NULL,
    payment_order_code BIGINT,
    amount             NUMERIC(19, 2),
    currency           VARCHAR(10),
    status             VARCHAR(50),                  -- PENDING | PAID | CANCELLED
    payment_method     VARCHAR(50),
    description        TEXT,
    expires_at         TIMESTAMP,
    paid_at            TIMESTAMP,
    reference          VARCHAR(200),
    created_at         TIMESTAMP,
    updated_at         TIMESTAMP,
    created_by         BIGINT,
    raw_response       TEXT,

    CONSTRAINT fk_pt_created_by
        FOREIGN KEY (created_by) REFERENCES users (id)
);

-- ============================================================
-- TABLE: payment_transaction_orders  (PayOS – links txn ↔ order)
-- ============================================================
CREATE TABLE IF NOT EXISTS payment_transaction_orders (
    id                      BIGSERIAL PRIMARY KEY,
    payment_transaction_id  BIGINT         NOT NULL,
    order_id                BIGINT         NOT NULL,
    amount_applied          NUMERIC(19, 2),
    created_at              TIMESTAMP,

    CONSTRAINT fk_pto_transaction
        FOREIGN KEY (payment_transaction_id)
        REFERENCES payment_transactions (id) ON DELETE CASCADE,
    CONSTRAINT fk_pto_order
        FOREIGN KEY (order_id) REFERENCES orders (id)
);

-- ============================================================
-- TABLE: wallets
-- ============================================================
CREATE TABLE IF NOT EXISTS wallets (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT         UNIQUE NOT NULL,
    balance    NUMERIC(19, 2) DEFAULT 0,
    currency   VARCHAR(10),
    status     VARCHAR(20),                           -- ACTIVE | FROZEN | CLOSED
    created_at TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_wallet_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

COMMENT ON COLUMN wallets.status IS 'ACTIVE, FROZEN, CLOSED';

-- ============================================================
-- TABLE: wallet_transactions
-- ============================================================
CREATE TABLE IF NOT EXISTS wallet_transactions (
    id             BIGSERIAL PRIMARY KEY,
    wallet_id      BIGINT         NOT NULL,
    amount         NUMERIC(19, 2),
    before_balance NUMERIC(19, 2),
    after_balance  NUMERIC(19, 2),
    type           VARCHAR(50),                       -- TOP_UP | WITHDRAW | PAYMENT | REFUND
    description    TEXT,
    created_at     TIMESTAMP,

    CONSTRAINT fk_wt_wallet
        FOREIGN KEY (wallet_id) REFERENCES wallets (id) ON DELETE CASCADE
);

COMMENT ON COLUMN wallet_transactions.type IS 'TOP_UP, WITHDRAW, PAYMENT, REFUND';

-- ============================================================
-- TABLE: password_reset_tokens
-- ============================================================
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_password_reset_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ============================================================
-- INDEXES
-- ============================================================

-- orders
CREATE INDEX IF NOT EXISTS idx_orders_payment_status ON orders (payment_status);
CREATE INDEX IF NOT EXISTS idx_orders_status         ON orders (status);
CREATE INDEX IF NOT EXISTS idx_orders_table_id       ON orders (table_id);
CREATE INDEX IF NOT EXISTS idx_orders_customer_id    ON orders (customer_id);

-- order_items
CREATE INDEX IF NOT EXISTS idx_order_items_order_id    ON order_items (order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_confirmed   ON order_items (is_confirmed);
CREATE INDEX IF NOT EXISTS idx_order_items_round       ON order_items (round_number);

-- bookings
CREATE INDEX IF NOT EXISTS idx_bookings_customer_id  ON bookings (customer_id);
CREATE INDEX IF NOT EXISTS idx_bookings_table_id     ON bookings (table_id);
CREATE INDEX IF NOT EXISTS idx_bookings_date         ON bookings (booking_date);

-- payments
CREATE INDEX IF NOT EXISTS idx_payments_order_id     ON payments (order_id);

-- password_reset_tokens
CREATE INDEX IF NOT EXISTS idx_password_reset_token   ON password_reset_tokens (token);
CREATE INDEX IF NOT EXISTS idx_password_reset_user_id ON password_reset_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_expiry  ON password_reset_tokens (expiry_date);

-- ============================================================
-- SEED DATA (optional – default admin account)
-- password_hash below = BCrypt of 'admin123' (replace in prod!)
-- ============================================================
INSERT INTO users (username, password_hash, full_name, role, status, created_at, updated_at)
VALUES
    ('admin', '$2a$10$xDOtWL.3fJvvGdOeJUqUCOT5/XRDvjHQWoFMC0q15h6CPLH5sWAFC',
     'System Admin', 'ADMIN', 'ACTIVE', NOW(), NOW())
ON CONFLICT (username) DO NOTHING;
