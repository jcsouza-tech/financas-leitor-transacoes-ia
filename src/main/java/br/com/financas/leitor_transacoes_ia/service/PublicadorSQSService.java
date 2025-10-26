package br.com.financas.leitor_transacoes_ia.service;

import br.com.financas.leitor_transacoes_ia.config.SQSConfig;
import br.com.financas.leitor_transacoes_ia.model.dto.TransacaoAIDTO;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicadorSQSService {
    
    private final SqsTemplate sqsTemplate;
    
    /**
     * Publica transações classificadas no SQS
     */
    public void publicarTransacoes(TransacaoAIDTO transacoes, String banco, String tipoDocumento) {
        log.info("Publicando {} transações no SQS. Banco: {}, Tipo: {}", 
                transacoes.getTotalTransacoes(), banco, tipoDocumento);
        
        try {
            sqsTemplate.send(SQSConfig.TRANSACOES_QUEUE, transacoes);
            log.info("Transações publicadas com sucesso no SQS");
        } catch (Exception e) {
            log.error("Erro ao publicar transações no SQS: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao publicar transações no SQS", e);
        }
    }
}
