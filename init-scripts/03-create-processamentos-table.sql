-- Script para criar a tabela de processamentos
-- Controla o status e progresso dos processamentos assíncronos

CREATE TABLE IF NOT EXISTS processamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    processamento_id VARCHAR(255) NOT NULL UNIQUE,
    nome_arquivo VARCHAR(255) NOT NULL,
    banco VARCHAR(100) NOT NULL,
    moeda VARCHAR(10) NOT NULL DEFAULT 'BRL',
    tipo_documento VARCHAR(50) NOT NULL,
    status ENUM('PENDENTE', 'PROCESSANDO', 'CONCLUIDO', 'ERRO', 'CANCELADO') NOT NULL DEFAULT 'PENDENTE',
    progresso INT DEFAULT 0,
    data_inicio TIMESTAMP NULL,
    data_fim TIMESTAMP NULL,
    transacoes_processadas INT DEFAULT 0,
    transacoes_salvas INT DEFAULT 0,
    duplicatas_ignoradas INT DEFAULT 0,
    tempo_processamento_ms BIGINT NULL,
    velocidade_processamento DECIMAL(10,2) NULL,
    mensagem TEXT NULL,
    erro TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Índices para melhor performance
CREATE INDEX idx_processamentos_status ON processamentos (status);
CREATE INDEX idx_processamentos_banco ON processamentos (banco);
CREATE INDEX idx_processamentos_data_inicio ON processamentos (data_inicio);
CREATE INDEX idx_processamentos_processamento_id ON processamentos (processamento_id);
