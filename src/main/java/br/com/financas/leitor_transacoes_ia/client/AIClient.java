package br.com.financas.leitor_transacoes_ia.client;

import br.com.financas.leitor_transacoes_ia.model.dto.TransacaoAIDTO;

public interface AIClient {
    
    /**
     * Processa um documento financeiro usando IA para extrair e classificar transações
     * 
     * @param textoExtraido Texto extraído do PDF/CSV
     * @param banco Nome do banco ou instituição financeira
     * @param moeda Moeda do documento (BRL, USD, EUR, etc.)
     * @param tipoDocumento Tipo do documento (EXTRATO, FATURA_CARTAO)
     * @return TransacaoAIDTO com as transações classificadas
     */
    TransacaoAIDTO processarDocumento(String textoExtraido, String banco, String moeda, String tipoDocumento);
}
