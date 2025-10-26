package br.com.financas.leitor_transacoes_ia.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@Slf4j
public class PDFTextExtractor {
    
    /**
     * Extrai texto de um arquivo PDF
     * 
     * @param arquivo Arquivo PDF
     * @return Texto extraído do PDF
     * @throws IOException Se houver erro na leitura do PDF
     */
    public String extrairTexto(MultipartFile arquivo) throws IOException {
        log.info("Iniciando extração de texto do PDF: {}", arquivo.getOriginalFilename());
        try (PDDocument document = PDDocument.load(arquivo.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            
            String texto = stripper.getText(document);
            
            log.info("Texto extraído com sucesso. Tamanho: {} caracteres", texto.length());
            return texto;
        } catch (Exception e) {
            log.error("Erro ao extrair texto do PDF: {}", e.getMessage(), e);
            throw new IOException("Erro ao processar arquivo PDF", e);
        }
    }
}
