package br.com.financas.leitor_transacoes_ia.service;

import br.com.financas.leitor_transacoes_ia.client.AIClient;
import br.com.financas.leitor_transacoes_ia.model.dto.TransacaoAIDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIClassificadorService {
    
    private final AIClient aiClient;
    
    /**
     * Processa um documento financeiro usando IA para extrair e classificar transações
     * 
     * @param textoExtraido Texto extraído do PDF/CSV
     * @param banco Nome do banco ou instituição financeira
     * @param moeda Moeda do documento (BRL, USD, EUR, etc.)
     * @param tipoDocumento Tipo do documento (EXTRATO, FATURA_CARTAO)
     * @return TransacaoAIDTO com as transações classificadas
     */
    public TransacaoAIDTO processarDocumento(String textoExtraido, String banco, String moeda, String tipoDocumento) {
        log.info("Iniciando classificação com IA. Banco: {}, Moeda: {}, Tipo: {}, Tamanho texto: {}", 
                banco, moeda, tipoDocumento, textoExtraido.length());
        
        try {
            TransacaoAIDTO resultado = aiClient.processarDocumento(textoExtraido, banco, moeda, tipoDocumento);
            
            log.info("Classificação concluída. Total de transações: {}", 
                    resultado.getTotalTransacoes());
            
            return resultado;
        } catch (Exception e) {
            log.error("Erro ao processar documento com IA: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao classificar transações com IA", e);
        }
    }
}
