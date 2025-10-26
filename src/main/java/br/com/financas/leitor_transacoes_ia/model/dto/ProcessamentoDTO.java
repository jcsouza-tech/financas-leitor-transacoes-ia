package br.com.financas.leitor_transacoes_ia.model.dto;

import br.com.financas.leitor_transacoes_ia.model.entity.Processamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class ProcessamentoDTO extends RepresentationModel<ProcessamentoDTO> {
    private Long id;
    private String processamentoId;
    private String nomeArquivo;
    private String banco;
    private String moeda;
    private String tipoDocumento;
    private Processamento.StatusProcessamento status;
    private Integer progresso;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private Integer transacoesProcessadas;
    private Integer transacoesSalvas;
    private Integer duplicatasIgnoradas;
    private Long tempoProcessamentoMs;
    private Double velocidadeProcessamento;
    private String mensagem;
    private String erro;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}