package br.com.financas.leitor_transacoes_ia.client;

import br.com.financas.leitor_transacoes_ia.model.dto.TransacaoAIDTO;
import br.com.financas.leitor_transacoes_ia.model.dto.TransacaoItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "placeholder", matchIfMissing = true)
@Slf4j
public class AIClientPlaceholderImpl implements AIClient {

    @Override
    public TransacaoAIDTO processarDocumento(String textoExtraido, String banco, String moeda, String tipoDocumento) {
        log.info("Processando documento com IA placeholder. Banco: {}, Moeda: {}, Tipo: {}, Tamanho texto: {}", 
                banco, moeda, tipoDocumento, textoExtraido.length());
        
        // Implementação placeholder que retorna dados de exemplo
        // Em produção, isso seria substituído por uma implementação real (OpenAI, Claude, etc.)
        
        TransacaoItemDTO transacao1 = TransacaoItemDTO.builder()
                .data(LocalDate.now().minusDays(1))
                .lancamento("COMPRA")
                .detalhes("SUPERMERCADO EXEMPLO")
                .numeroDocumento("123456")
                .valor(new BigDecimal("150.50"))
                .tipoLancamento("DEBITO")
                .categoria("ALIMENTACAO")
                .tipoDocumento(tipoDocumento)
                .moeda(moeda)
                .build();
                
        TransacaoItemDTO transacao2 = TransacaoItemDTO.builder()
                .data(LocalDate.now().minusDays(2))
                .lancamento("PAGAMENTO")
                .detalhes("CONTA DE LUZ")
                .numeroDocumento("789012")
                .valor(new BigDecimal("89.90"))
                .tipoLancamento("DEBITO")
                .categoria("MORADIA")
                .tipoDocumento(tipoDocumento)
                .moeda(moeda)
                .build();
        
        return TransacaoAIDTO.builder()
                .banco(banco)
                .moeda(moeda)
                .tipoDocumento(tipoDocumento)
                .totalTransacoes(2)
                .transacoes(Arrays.asList(transacao1, transacao2))
                .build();
    }
}
