-- Migration V4: Add multi-tenancy (user_id) to tables
-- Adds user_id field for data isolation by user

-- Add user_id to transacoes table
ALTER TABLE transacoes ADD COLUMN user_id VARCHAR(255);
ALTER TABLE transacoes ADD INDEX idx_user_id (user_id);
ALTER TABLE transacoes ADD INDEX idx_user_banco (user_id, banco);

-- Add user_id to processamentos table
ALTER TABLE processamentos ADD COLUMN user_id VARCHAR(255);
ALTER TABLE processamentos ADD INDEX idx_user_id (user_id);

-- Comments about indexes:
-- idx_user_id: For user-specific queries
-- idx_user_banco: For user and bank queries (optimization)
