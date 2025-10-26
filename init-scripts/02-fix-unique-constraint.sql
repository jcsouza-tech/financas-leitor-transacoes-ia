-- Script para remover constraint única da tabela transacoes
-- Permite transações similares no mesmo dia (ex: passagem ida/volta)

-- Remover a constraint antiga se existir
ALTER TABLE transacoes DROP INDEX IF EXISTS uk_transacao_unique;

-- Adicionar índices para melhor performance nas consultas
CREATE INDEX idx_transacao_busca ON transacoes (data, lancamento, valor, banco);
CREATE INDEX idx_transacao_documento ON transacoes (data, numero_documento, valor, banco);
CREATE INDEX idx_transacao_detalhes ON transacoes (data, lancamento, valor, banco, detalhes);
