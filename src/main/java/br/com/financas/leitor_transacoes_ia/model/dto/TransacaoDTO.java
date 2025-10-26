package br.com.financas.leitor_transacoes_ia.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class TransacaoDTO extends RepresentationModel<TransacaoDTO> {
    private Long id;
    private LocalDate data;
    private String lancamento;
    private String detalhes;
    private String numeroDocumento;
    private BigDecimal valor;
    private String moeda;
    private String tipoLancamento;
    private String categoria;
    private String banco;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}