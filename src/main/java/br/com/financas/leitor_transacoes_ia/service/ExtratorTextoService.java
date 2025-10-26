package br.com.financas.leitor_transacoes_ia.service;

import br.com.financas.leitor_transacoes_ia.parser.CSVTextExtractor;
import br.com.financas.leitor_transacoes_ia.parser.PDFTextExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtratorTextoService {
    
    private final PDFTextExtractor pdfTextExtractor;
    private final CSVTextExtractor csvTextExtractor;
    
    /**
     * Extrai texto de um arquivo PDF ou CSV
     * 
     * @param arquivo Arquivo a ser processado
     * @return Texto extraído do arquivo
     * @throws IOException Se houver erro na leitura do arquivo
     */
    public String extrairTexto(MultipartFile arquivo) throws IOException {
        String nomeArquivo = arquivo.getOriginalFilename();
        String tipoArquivo = arquivo.getContentType();
        
        log.info("Extraindo texto do arquivo: {} (tipo: {})", nomeArquivo, tipoArquivo);
        
        if (tipoArquivo != null && tipoArquivo.equals("application/pdf")) {
            return pdfTextExtractor.extrairTexto(arquivo);
        } else if (tipoArquivo != null && tipoArquivo.equals("text/csv")) {
            return csvTextExtractor.extrairTexto(arquivo);
        } else {
            throw new IllegalArgumentException("Tipo de arquivo não suportado: " + tipoArquivo);
        }
    }
    
    /**
     * Valida se o arquivo é suportado
     * 
     * @param arquivo Arquivo a ser validado
     * @return true se o arquivo é suportado
     */
    public boolean isArquivoSuportado(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            return false;
        }
        
        String tipoArquivo = arquivo.getContentType();
        return "application/pdf".equals(tipoArquivo) || "text/csv".equals(tipoArquivo);
    }
    
    /**
     * Valida o tamanho do arquivo
     * 
     * @param arquivo Arquivo a ser validado
     * @param tamanhoMaximo Tamanho máximo em bytes
     * @return true se o arquivo está dentro do limite
     */
    public boolean isTamanhoValido(MultipartFile arquivo, long tamanhoMaximo) {
        return arquivo.getSize() <= tamanhoMaximo;
    }
}
