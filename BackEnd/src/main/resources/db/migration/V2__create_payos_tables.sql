-- Flyway migration: create tables for PayOS integration

CREATE TABLE IF NOT EXISTS payment_transactions (
  id BIGSERIAL PRIMARY KEY,
  payos_payment_id VARCHAR(200) UNIQUE,
  internal_reference VARCHAR(200) UNIQUE NOT NULL,
  amount NUMERIC(19,2),
  currency VARCHAR(10),
  status VARCHAR(50),
  payment_method VARCHAR(50),
  description TEXT,
  expires_at TIMESTAMP,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  created_by BIGINT,
  raw_response TEXT,
  CONSTRAINT fk_pt_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS payment_transaction_orders (
  id BIGSERIAL PRIMARY KEY,
  payment_transaction_id BIGINT NOT NULL,
  order_id BIGINT NOT NULL,
  amount_applied NUMERIC(19,2),
  created_at TIMESTAMP,
  CONSTRAINT fk_pto_transaction FOREIGN KEY (payment_transaction_id) REFERENCES payment_transactions(id) ON DELETE CASCADE,
  CONSTRAINT fk_pto_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE IF NOT EXISTS wallets (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT UNIQUE NOT NULL,
  balance NUMERIC(19,2) DEFAULT 0,
  currency VARCHAR(10),
  status VARCHAR(20),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS wallet_transactions (
  id BIGSERIAL PRIMARY KEY,
  wallet_id BIGINT NOT NULL,
  amount NUMERIC(19,2),
  before_balance NUMERIC(19,2),
  after_balance NUMERIC(19,2),
  type VARCHAR(50),
  description TEXT,
  created_at TIMESTAMP,
  CONSTRAINT fk_wt_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE
);

