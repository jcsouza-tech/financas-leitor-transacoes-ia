-- Script para adicionar multi-tenancy (user_id) nas tabelas
-- Este script adiciona o campo user_id para isolamento de dados por usuário

-- Adicionar user_id na tabela transacoes
ALTER TABLE transacoes ADD COLUMN user_id VARCHAR(255);
ALTER TABLE transacoes ADD INDEX idx_user_id (user_id);
ALTER TABLE transacoes ADD INDEX idx_user_banco (user_id, banco);

-- Adicionar user_id na tabela processamentos
ALTER TABLE processamentos ADD COLUMN user_id VARCHAR(255);
ALTER TABLE processamentos ADD INDEX idx_user_id (user_id);

-- Comentários sobre os índices:
-- idx_user_id: Para consultas por usuário
-- idx_user_banco: Para consultas por usuário e banco (otimização)
