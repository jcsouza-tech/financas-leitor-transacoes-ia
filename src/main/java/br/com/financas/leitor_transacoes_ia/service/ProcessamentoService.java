package br.com.financas.leitor_transacoes_ia.service;

import br.com.financas.leitor_transacoes_ia.model.entity.Processamento;
import br.com.financas.leitor_transacoes_ia.repository.ProcessamentoRepository;
import br.com.financas.leitor_transacoes_ia.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessamentoService {

    private final ProcessamentoRepository processamentoRepository;

    /**
     * Cria um novo processamento
     */
    @Transactional
    public Processamento criarProcessamento(String nomeArquivo, String banco, String moeda, String tipoDocumento) {
        String processamentoId = UUID.randomUUID().toString();
        String userId = UserContext.getCurrentUserId();
        
        Processamento processamento = Processamento.builder()
                .processamentoId(processamentoId)
                .nomeArquivo(nomeArquivo)
                .banco(banco)
                .moeda(moeda)
                .tipoDocumento(tipoDocumento)
                .status(Processamento.StatusProcessamento.PENDENTE)
                .progresso(0)
                .dataInicio(LocalDateTime.now())
                .userId(userId)
                .build();

        Processamento saved = processamentoRepository.save(processamento);
        log.info("Processamento criado: {} - {} (usuário: {})", processamentoId, nomeArquivo, userId);
        
        return saved;
    }

    /**
     * Atualiza o status de um processamento
     */
    @Transactional
    public Processamento atualizarStatus(String processamentoId, Processamento.StatusProcessamento status) {
        String userId = UserContext.getCurrentUserId();
        Optional<Processamento> optional = processamentoRepository.findByUserIdAndProcessamentoId(userId, processamentoId);
        
        if (optional.isPresent()) {
            Processamento processamento = optional.get();
            processamento.setStatus(status);
            processamento.setUpdatedAt(LocalDateTime.now());
            
            if (status == Processamento.StatusProcessamento.PROCESSANDO) {
                processamento.setDataInicio(LocalDateTime.now());
            } else if (status == Processamento.StatusProcessamento.CONCLUIDO || 
                      status == Processamento.StatusProcessamento.ERRO ||
                      status == Processamento.StatusProcessamento.CANCELADO) {
                processamento.setDataFim(LocalDateTime.now());
                
                // Calcular tempo de processamento
                if (processamento.getDataInicio() != null) {
                    long tempoMs = java.time.Duration.between(processamento.getDataInicio(), processamento.getDataFim()).toMillis();
                    processamento.setTempoProcessamentoMs(tempoMs);
                }
            }
            
            Processamento saved = processamentoRepository.save(processamento);
            log.info("Status atualizado: {} - {}", processamentoId, status);
            
            return saved;
        }
        
        throw new RuntimeException("Processamento não encontrado: " + processamentoId);
    }

    /**
     * Atualiza o progresso de um processamento
     */
    @Transactional
    public Processamento atualizarProgresso(String processamentoId, Integer progresso) {
        String userId = UserContext.getCurrentUserId();
        Optional<Processamento> optional = processamentoRepository.findByUserIdAndProcessamentoId(userId, processamentoId);
        
        if (optional.isPresent()) {
            Processamento processamento = optional.get();
            processamento.setProgresso(progresso);
            processamento.setUpdatedAt(LocalDateTime.now());
            
            Processamento saved = processamentoRepository.save(processamento);
            log.debug("Progresso atualizado: {} - {}%", processamentoId, progresso);
            
            return saved;
        }
        
        throw new RuntimeException("Processamento não encontrado: " + processamentoId);
    }

    /**
     * Atualiza estatísticas de um processamento
     */
    @Transactional
    public Processamento atualizarEstatisticas(String processamentoId, 
                                               Integer transacoesProcessadas, 
                                               Integer transacoesSalvas, 
                                               Integer duplicatasIgnoradas) {
        String userId = UserContext.getCurrentUserId();
        Optional<Processamento> optional = processamentoRepository.findByUserIdAndProcessamentoId(userId, processamentoId);
        
        if (optional.isPresent()) {
            Processamento processamento = optional.get();
            processamento.setTransacoesProcessadas(transacoesProcessadas);
            processamento.setTransacoesSalvas(transacoesSalvas);
            processamento.setDuplicatasIgnoradas(duplicatasIgnoradas);
            processamento.setUpdatedAt(LocalDateTime.now());
            
            // Calcular velocidade de processamento
            if (processamento.getTempoProcessamentoMs() != null && processamento.getTempoProcessamentoMs() > 0) {
                double velocidade = (double) transacoesProcessadas / (processamento.getTempoProcessamentoMs() / 1000.0);
                processamento.setVelocidadeProcessamento(velocidade);
            }
            
            Processamento saved = processamentoRepository.save(processamento);
            log.info("Estatísticas atualizadas: {} - Processadas: {}, Salvas: {}, Duplicatas: {}", 
                    processamentoId, transacoesProcessadas, transacoesSalvas, duplicatasIgnoradas);
            
            return saved;
        }
        
        throw new RuntimeException("Processamento não encontrado: " + processamentoId);
    }

    /**
     * Adiciona mensagem de erro
     */
    @Transactional
    public Processamento adicionarErro(String processamentoId, String erro) {
        String userId = UserContext.getCurrentUserId();
        Optional<Processamento> optional = processamentoRepository.findByUserIdAndProcessamentoId(userId, processamentoId);
        
        if (optional.isPresent()) {
            Processamento processamento = optional.get();
            processamento.setErro(erro);
            processamento.setStatus(Processamento.StatusProcessamento.ERRO);
            processamento.setDataFim(LocalDateTime.now());
            processamento.setUpdatedAt(LocalDateTime.now());
            
            Processamento saved = processamentoRepository.save(processamento);
            log.error("Erro adicionado ao processamento {}: {}", processamentoId, erro);
            
            return saved;
        }
        
        throw new RuntimeException("Processamento não encontrado: " + processamentoId);
    }

    /**
     * Lista todos os processamentos do usuário atual
     */
    public List<Processamento> listarProcessamentos() {
        String userId = UserContext.getCurrentUserId();
        return processamentoRepository.findByUserId(userId);
    }

    /**
     * Busca processamento por ID (do usuário atual)
     */
    public Optional<Processamento> buscarPorId(String processamentoId) {
        String userId = UserContext.getCurrentUserId();
        return processamentoRepository.findByUserIdAndProcessamentoId(userId, processamentoId);
    }

    /**
     * Lista processamentos por status (do usuário atual)
     */
    public List<Processamento> listarPorStatus(Processamento.StatusProcessamento status) {
        String userId = UserContext.getCurrentUserId();
        return processamentoRepository.findByUserIdAndStatus(userId, status);
    }

    /**
     * Cancela um processamento
     */
    @Transactional
    public Processamento cancelarProcessamento(String processamentoId) {
        String userId = UserContext.getCurrentUserId();
        Optional<Processamento> optional = processamentoRepository.findByUserIdAndProcessamentoId(userId, processamentoId);
        
        if (optional.isPresent()) {
            Processamento processamento = optional.get();
            
            if (processamento.getStatus() == Processamento.StatusProcessamento.PENDENTE) {
                processamento.setStatus(Processamento.StatusProcessamento.CANCELADO);
                processamento.setDataFim(LocalDateTime.now());
                processamento.setUpdatedAt(LocalDateTime.now());
                
                Processamento saved = processamentoRepository.save(processamento);
                log.info("Processamento cancelado: {}", processamentoId);
                
                return saved;
            } else {
                throw new RuntimeException("Processamento não pode ser cancelado no status atual: " + processamento.getStatus());
            }
        }
        
        throw new RuntimeException("Processamento não encontrado: " + processamentoId);
    }
}
