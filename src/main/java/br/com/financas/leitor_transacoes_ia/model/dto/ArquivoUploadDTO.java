package br.com.financas.leitor_transacoes_ia.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArquivoUploadDTO {
    
    private String nomeArquivo;
    private String tipoArquivo;
    private Long tamanhoArquivo;
    private String banco;
    private String moeda; // BRL, USD, EUR, etc.
    private String tipoDocumento; // EXTRATO, FATURA_CARTAO
    private LocalDateTime dataUpload;
    private String requestId;
    private String status; // PROCESSANDO, CONCLUIDO, ERRO
}
