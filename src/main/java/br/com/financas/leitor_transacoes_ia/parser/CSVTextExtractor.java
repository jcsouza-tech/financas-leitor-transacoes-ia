package br.com.financas.leitor_transacoes_ia.parser;

import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;

@Component
@Slf4j
public class CSVTextExtractor {
    
    /**
     * Extrai texto de um arquivo CSV convertendo para formato legível
     * 
     * @param arquivo Arquivo CSV
     * @return Texto extraído do CSV
     * @throws IOException Se houver erro na leitura do CSV
     */
    public String extrairTexto(MultipartFile arquivo) throws IOException {
        log.info("Iniciando extração de texto do CSV: {}", arquivo.getOriginalFilename());
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(arquivo.getInputStream()))) {
            List<String[]> linhas = reader.readAll();
            
            StringWriter writer = new StringWriter();
            
            for (String[] linha : linhas) {
                for (int i = 0; i < linha.length; i++) {
                    writer.write(linha[i]);
                    if (i < linha.length - 1) {
                        writer.write(" | ");
                    }
                }
                writer.write("\n");
            }
            
            String texto = writer.toString();
            log.info("Texto extraído com sucesso. Tamanho: {} caracteres", texto.length());
            return texto;
        } catch (Exception e) {
            log.error("Erro ao extrair texto do CSV: {}", e.getMessage(), e);
            throw new IOException("Erro ao processar arquivo CSV", e);
        }
    }
}
