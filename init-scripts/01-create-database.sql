-- Script de inicialização do banco de dados para Leitor Transações IA
-- Este script cria as tabelas necessárias para a aplicação

USE financas_prd_mysql;

-- Tabela de transações
CREATE TABLE IF NOT EXISTS transacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    data DATE NOT NULL,
    lancamento VARCHAR(255) NOT NULL,
    detalhes TEXT,
    numero_documento VARCHAR(100),
    valor DECIMAL(15,2) NOT NULL,
    tipo_lancamento VARCHAR(50) NOT NULL,
    categoria VARCHAR(100),
    tipo_documento VARCHAR(50) NOT NULL,
    moeda VARCHAR(10) NOT NULL DEFAULT 'BRL',
    banco VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_data (data),
    INDEX idx_categoria (categoria),
    INDEX idx_tipo_lancamento (tipo_lancamento),
    INDEX idx_banco (banco)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela para logs de processamento
CREATE TABLE IF NOT EXISTS processamento_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id VARCHAR(255) NOT NULL,
    arquivo_nome VARCHAR(255) NOT NULL,
    banco VARCHAR(100),
    moeda VARCHAR(10),
    tipo_documento VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    total_transacoes INT DEFAULT 0,
    erro TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_request_id (request_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
