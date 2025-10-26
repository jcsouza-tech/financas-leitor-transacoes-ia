package br.com.financas.leitor_transacoes_ia.repository;

import br.com.financas.leitor_transacoes_ia.model.entity.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    
    List<Transacao> findByBanco(String banco);
    
    List<Transacao> findByDataBetween(LocalDate dataInicio, LocalDate dataFim);
    
    List<Transacao> findByBancoAndDataBetween(String banco, LocalDate dataInicio, LocalDate dataFim);
    
    /**
     * Verifica se existe uma transação com os mesmos dados básicos
     */
    Optional<Transacao> findByDataAndLancamentoAndValorAndBanco(
            LocalDate data, String lancamento, BigDecimal valor, String banco);
    
    /**
     * Verifica duplicata por número de documento (mais preciso)
     */
    Optional<Transacao> findByDataAndNumeroDocumentoAndValorAndBanco(
            LocalDate data, String numeroDocumento, BigDecimal valor, String banco);
    
    /**
     * Verifica duplicata exata (incluindo detalhes)
     */
    Optional<Transacao> findByDataAndLancamentoAndValorAndBancoAndDetalhes(
            LocalDate data, String lancamento, BigDecimal valor, String banco, String detalhes);
    
    // Queries com filtro por userId (multi-tenancy)
    List<Transacao> findByUserId(String userId);
    
    List<Transacao> findByUserIdAndBanco(String userId, String banco);
    
    List<Transacao> findByUserIdAndDataBetween(String userId, LocalDate dataInicio, LocalDate dataFim);
    
    List<Transacao> findByUserIdAndBancoAndDataBetween(String userId, String banco, LocalDate dataInicio, LocalDate dataFim);
    
    /**
     * Verifica duplicata por usuário (multi-tenancy)
     */
    Optional<Transacao> findByUserIdAndDataAndLancamentoAndValorAndBanco(
            String userId, LocalDate data, String lancamento, BigDecimal valor, String banco);
    
    /**
     * Verifica duplicata por número de documento e usuário
     */
    Optional<Transacao> findByUserIdAndDataAndNumeroDocumentoAndValorAndBanco(
            String userId, LocalDate data, String numeroDocumento, BigDecimal valor, String banco);
    
    /**
     * Verifica duplicata exata por usuário (incluindo detalhes)
     */
    Optional<Transacao> findByUserIdAndDataAndLancamentoAndValorAndBancoAndDetalhes(
            String userId, LocalDate data, String lancamento, BigDecimal valor, String banco, String detalhes);
}
