package br.com.financas.leitor_transacoes_ia.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "processamentos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Processamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "processamento_id", unique = true, nullable = false)
    private String processamentoId;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(name = "banco", nullable = false)
    private String banco;

    @Column(name = "moeda", nullable = false)
    private String moeda;

    @Column(name = "tipo_documento", nullable = false)
    private String tipoDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusProcessamento status;

    @Column(name = "progresso")
    private Integer progresso;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Column(name = "transacoes_processadas")
    private Integer transacoesProcessadas;

    @Column(name = "transacoes_salvas")
    private Integer transacoesSalvas;

    @Column(name = "duplicatas_ignoradas")
    private Integer duplicatasIgnoradas;

    @Column(name = "tempo_processamento_ms")
    private Long tempoProcessamentoMs;

    @Column(name = "velocidade_processamento")
    private Double velocidadeProcessamento;

    @Column(name = "mensagem")
    private String mensagem;

    @Column(name = "erro")
    private String erro;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (this.moeda == null) {
            this.moeda = "BRL";
        }
        if (this.status == null) {
            this.status = StatusProcessamento.PENDENTE;
        }
        if (this.progresso == null) {
            this.progresso = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum StatusProcessamento {
        PENDENTE,
        PROCESSANDO,
        CONCLUIDO,
        ERRO,
        CANCELADO
    }
}
