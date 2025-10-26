package br.com.financas.leitor_transacoes_ia.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoAIDTO {
    
    private String processamentoId; // ID do processamento para controle
    private String banco;
    private String moeda; // BRL, USD, EUR, etc.
    private String tipoDocumento; // EXTRATO, FATURA_CARTAO
    private Integer totalTransacoes;
    private List<TransacaoItemDTO> transacoes;
}
