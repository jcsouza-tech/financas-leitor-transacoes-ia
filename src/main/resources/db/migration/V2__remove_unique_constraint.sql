-- Migration V2: Remove unique constraint and add performance indexes
-- Allows similar transactions on the same day (e.g., round trip tickets)

-- Remove the old constraint if it exists
ALTER TABLE transacoes DROP INDEX IF EXISTS uk_transacao_unique;

-- Add indexes for better query performance
CREATE INDEX idx_transacao_busca ON transacoes (data, lancamento, valor, banco);
CREATE INDEX idx_transacao_documento ON transacoes (data, numero_documento, valor, banco);
CREATE INDEX idx_transacao_detalhes ON transacoes (data, lancamento, valor, banco, detalhes);
