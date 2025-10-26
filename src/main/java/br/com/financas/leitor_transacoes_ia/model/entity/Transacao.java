package br.com.financas.leitor_transacoes_ia.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transacoes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private String lancamento;

    private String detalhes;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "valor", nullable = false, precision = 10, scale = 4)
    private BigDecimal valor;

    @Column(name = "moeda", nullable = false, length = 3)
    private String moeda;

    @Column(name = "tipo_lancamento")
    private String tipoLancamento;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private String banco;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        updatedAt = LocalDate.now();
        if (this.moeda == null) {
            this.moeda = "BRL"; // Valor padrão
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }
}
