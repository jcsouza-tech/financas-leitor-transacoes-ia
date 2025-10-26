package br.com.financas.leitor_transacoes_ia.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoItemDTO {
    
    private LocalDate data;
    private String lancamento;
    private String detalhes;
    private String numeroDocumento;
    private BigDecimal valor;
    private String tipoLancamento;
    private String categoria;  // ← CLASSIFICAÇÃO DA IA
    private String tipoDocumento; // EXTRATO, FATURA_CARTAO
    private String moeda; // BRL, USD, EUR, etc.
}
