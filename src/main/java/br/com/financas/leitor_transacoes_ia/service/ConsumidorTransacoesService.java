package br.com.financas.leitor_transacoes_ia.service;

import br.com.financas.leitor_transacoes_ia.config.SQSConfig;
import br.com.financas.leitor_transacoes_ia.model.dto.TransacaoAIDTO;
import br.com.financas.leitor_transacoes_ia.model.dto.TransacaoItemDTO;
import br.com.financas.leitor_transacoes_ia.model.entity.Transacao;
import br.com.financas.leitor_transacoes_ia.model.entity.Processamento;
import br.com.financas.leitor_transacoes_ia.repository.TransacaoRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumidorTransacoesService {
    
    private final TransacaoRepository transacaoRepository;
    private final ProcessamentoService processamentoService;
    
    /**
     * Consome transações do SQS e salva no banco de dados
     * 
     * @param transacoesAI Transações classificadas pela IA
     */
    @SqsListener(value = SQSConfig.TRANSACOES_QUEUE)
    public void processarTransacoes(TransacaoAIDTO transacoesAI) {
        String processamentoId = transacoesAI.getProcessamentoId();
        log.info("Processando {} transações do banco: {} - Processamento ID: {}", 
                transacoesAI.getTotalTransacoes(), transacoesAI.getBanco(), processamentoId);
        
        try {
            // Obter userId do processamento
            String userId = null;
            if (processamentoId != null) {
                var processamento = processamentoService.buscarPorId(processamentoId);
                if (processamento.isPresent()) {
                    userId = processamento.get().getUserId();
                }
            }
            
            // Atualizar status para PROCESSANDO
            if (processamentoId != null) {
                processamentoService.atualizarStatus(processamentoId, Processamento.StatusProcessamento.PROCESSANDO);
            }
            
            int sucessos = 0;
            int erros = 0;
            int duplicatas = 0;
            int totalTransacoes = transacoesAI.getTransacoes().size();
            
            for (int i = 0; i < transacoesAI.getTransacoes().size(); i++) {
                TransacaoItemDTO item = transacoesAI.getTransacoes().get(i);
                
                try {
                    // Atualizar progresso
                    if (processamentoId != null) {
                        int progresso = (int) ((i + 1) * 100.0 / totalTransacoes);
                        processamentoService.atualizarProgresso(processamentoId, progresso);
                    }
                    
                    // Verificar se já existe uma transação similar
                    if (transacaoJaExiste(item, transacoesAI.getBanco(), userId)) {
                        duplicatas++;
                        log.warn("Transação duplicada ignorada: {} - {} - {} - {}", 
                                item.getData(), item.getLancamento(), item.getValor(), transacoesAI.getBanco());
                        continue;
                    }
                    
                    Transacao transacao = Transacao.builder()
                            .data(item.getData())
                            .lancamento(item.getLancamento())
                            .detalhes(item.getDetalhes())
                            .numeroDocumento(item.getNumeroDocumento())
                            .valor(item.getValor())
                            .moeda(item.getMoeda())
                            .tipoLancamento(item.getTipoLancamento())
                            .categoria(item.getCategoria())
                            .banco(transacoesAI.getBanco())
                            .userId(userId != null ? userId : "system") // Fallback para system se não encontrar userId
                            .build();
                    
                    transacaoRepository.save(transacao);
                    sucessos++;
                    log.debug("Transação salva: {} - {} - {}", 
                            item.getData(), item.getLancamento(), item.getValor());
                    
                } catch (Exception e) {
                    erros++;
                    log.error("Erro ao salvar transação individual: {} - {} - {} - Erro: {}", 
                            item.getData(), item.getLancamento(), item.getValor(), e.getMessage());
                    // Continua processando as outras transações
                }
            }
            
            // Atualizar estatísticas finais
            if (processamentoId != null) {
                processamentoService.atualizarEstatisticas(processamentoId, totalTransacoes, sucessos, duplicatas);
                processamentoService.atualizarStatus(processamentoId, Processamento.StatusProcessamento.CONCLUIDO);
            }
            
            log.info("Processamento concluído - Total: {}, Sucessos: {}, Erros: {}, Duplicatas: {}", 
                    totalTransacoes, sucessos, erros, duplicatas);
            
        } catch (Exception e) {
            log.error("Erro geral no processamento: {}", e.getMessage(), e);
            
            // Marcar como erro se houver processamentoId
            if (processamentoId != null) {
                processamentoService.adicionarErro(processamentoId, e.getMessage());
            }
        }
    }
    
    /**
     * Verifica se uma transação realmente duplicada já existe no banco
     * Só considera duplicata se tiver o mesmo número de documento (quando disponível)
     * ou se for exatamente idêntica em todos os campos
     */
    private boolean transacaoJaExiste(TransacaoItemDTO item, String banco, String userId) {
        try {
            // Se tem número de documento, verifica por ele (mais preciso)
            if (item.getNumeroDocumento() != null && !item.getNumeroDocumento().trim().isEmpty() 
                && !item.getNumeroDocumento().equals("null")) {
                
                return transacaoRepository.findByUserIdAndDataAndNumeroDocumentoAndValorAndBanco(
                        userId != null ? userId : "system",
                        item.getData(), 
                        item.getNumeroDocumento(),
                        item.getValor(), 
                        banco
                ).isPresent();
            }
            
            // Se não tem número de documento, verifica se é exatamente idêntica
            // (mesmo dia, lançamento, valor, banco E detalhes)
            return transacaoRepository.findByUserIdAndDataAndLancamentoAndValorAndBancoAndDetalhes(
                    userId != null ? userId : "system",
                    item.getData(), 
                    item.getLancamento(), 
                    item.getValor(), 
                    banco,
                    item.getDetalhes()
            ).isPresent();
            
        } catch (Exception e) {
            log.warn("Erro ao verificar duplicata: {}", e.getMessage());
            return false; // Se não conseguir verificar, permite salvar
        }
    }
}
