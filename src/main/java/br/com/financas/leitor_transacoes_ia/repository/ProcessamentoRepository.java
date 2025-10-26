package br.com.financas.leitor_transacoes_ia.repository;

import br.com.financas.leitor_transacoes_ia.model.entity.Processamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessamentoRepository extends JpaRepository<Processamento, Long> {
    
    Optional<Processamento> findByProcessamentoId(String processamentoId);
    
    List<Processamento> findByStatus(Processamento.StatusProcessamento status);
    
    List<Processamento> findByBanco(String banco);
    
    List<Processamento> findByDataInicioBetween(LocalDateTime dataInicio, LocalDateTime dataFim);
    
    @Query("SELECT p FROM Processamento p WHERE p.status IN :statuses ORDER BY p.dataInicio DESC")
    List<Processamento> findByStatusIn(@Param("statuses") List<Processamento.StatusProcessamento> statuses);
    
    @Query("SELECT COUNT(p) FROM Processamento p WHERE p.status = :status")
    Long countByStatus(@Param("status") Processamento.StatusProcessamento status);
    
    @Query("SELECT p FROM Processamento p WHERE p.status = 'PROCESSANDO' AND p.dataInicio < :limite ORDER BY p.dataInicio")
    List<Processamento> findProcessamentosEmAndamentoAntigos(@Param("limite") LocalDateTime limite);
    
    // Queries com filtro por userId (multi-tenancy)
    List<Processamento> findByUserId(String userId);
    
    Optional<Processamento> findByUserIdAndProcessamentoId(String userId, String processamentoId);
    
    List<Processamento> findByUserIdAndStatus(String userId, Processamento.StatusProcessamento status);
    
    List<Processamento> findByUserIdAndBanco(String userId, String banco);
    
    List<Processamento> findByUserIdAndDataInicioBetween(String userId, LocalDateTime dataInicio, LocalDateTime dataFim);
    
    @Query("SELECT p FROM Processamento p WHERE p.userId = :userId AND p.status IN :statuses ORDER BY p.dataInicio DESC")
    List<Processamento> findByUserIdAndStatusIn(@Param("userId") String userId, @Param("statuses") List<Processamento.StatusProcessamento> statuses);
    
    @Query("SELECT COUNT(p) FROM Processamento p WHERE p.userId = :userId AND p.status = :status")
    Long countByUserIdAndStatus(@Param("userId") String userId, @Param("status") Processamento.StatusProcessamento status);
}
