package br.com.financas.leitor_transacoes_ia.client;

import br.com.financas.leitor_transacoes_ia.model.dto.TransacaoAIDTO;
import br.com.financas.leitor_transacoes_ia.model.dto.TransacaoItemDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
@RequiredArgsConstructor
@Slf4j
public class GeminiClientImpl implements AIClient {

    private final ObjectMapper objectMapper;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.model:gemini-2.5-flash}")
    private String model;

    @Value("${ai.timeout:30000}")
    private int timeout;

    @Value("${ai.max-tokens:4000}")
    private int maxTokens;

    @Override
    public TransacaoAIDTO processarDocumento(String textoExtraido, String banco, String moeda, String tipoDocumento) {
        log.info("Processando documento com Gemini. Banco: {}, Moeda: {}, Tipo: {}, Tamanho texto: {}", 
                banco, moeda, tipoDocumento, textoExtraido.length());

        try {
            String prompt = construirPrompt(textoExtraido, banco, moeda, tipoDocumento);
            String resposta = chamarGemini(prompt);
            return processarRespostaGemini(resposta, banco, moeda, tipoDocumento);
        } catch (Exception e) {
            log.error("Erro ao processar documento com Gemini: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao processar documento com Gemini", e);
        }
    }

    private String construirPrompt(String textoExtraido, String banco, String moeda, String tipoDocumento) {
        return String.format("""
            Você é um especialista em análise de transações financeiras. Analise o texto abaixo extraído de um documento financeiro (%s) e retorne APENAS um JSON válido com as transações encontradas.

            Para cada transação, extraia:
            - data (formato: yyyy-MM-dd)
            - lancamento (descrição breve)
            - detalhes (descrição completa)
            - numeroDocumento (se houver)
            - valor (número decimal)
            - tipoLancamento (DEBITO, CREDITO, PAGAMENTO, COMPRA, etc.)
            - categoria (classifique em: ALIMENTACAO, TRANSPORTE, SAUDE, LAZER, MORADIA, EDUCACAO, OUTROS)

            Formato JSON esperado:
            {
              "banco": "%s",
              "moeda": "%s",
              "tipoDocumento": "%s",
              "totalTransacoes": 0,
              "transacoes": [
                {
                  "data": "2024-01-15",
                  "lancamento": "COMPRA",
                  "detalhes": "SUPERMERCADO XYZ",
                  "numeroDocumento": "123456",
                  "valor": 150.50,
                  "tipoLancamento": "DEBITO",
                  "categoria": "ALIMENTACAO"
                }
              ]
            }

            TEXTO DO DOCUMENTO:
            %s
            """, tipoDocumento, banco, moeda, tipoDocumento, textoExtraido);
    }

    private String chamarGemini(String prompt) {
        try {
            // Configurar o cliente Gemini
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();

            // Gerar conteúdo usando o SDK
            GenerateContentResponse response = client.models.generateContent(
                    model,
                    prompt,
                    null
            );

            // Retornar o texto gerado
            return response.text();

        } catch (Exception e) {
            log.error("Erro ao chamar Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao chamar Gemini API", e);
        }
    }

    private TransacaoAIDTO processarRespostaGemini(String resposta, String banco, String moeda, String tipoDocumento) {
        try {
            // Limpar o conteúdo para extrair apenas o JSON
            String content = resposta.trim();
            if (content.startsWith("```json")) {
                content = content.substring(7);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }
            content = content.trim();

            JsonNode transacoesNode = objectMapper.readTree(content);
            List<TransacaoItemDTO> transacoes = new ArrayList<>();

            JsonNode transacoesArray = transacoesNode.path("transacoes");
            if (transacoesArray.isArray()) {
                for (JsonNode transacaoNode : transacoesArray) {
                    TransacaoItemDTO transacao = TransacaoItemDTO.builder()
                            .data(LocalDate.parse(transacaoNode.path("data").asText(), DateTimeFormatter.ISO_LOCAL_DATE))
                            .lancamento(transacaoNode.path("lancamento").asText())
                            .detalhes(transacaoNode.path("detalhes").asText())
                            .numeroDocumento(transacaoNode.path("numeroDocumento").asText())
                            .valor(new BigDecimal(transacaoNode.path("valor").asText()))
                            .tipoLancamento(transacaoNode.path("tipoLancamento").asText())
                            .categoria(transacaoNode.path("categoria").asText())
                            .tipoDocumento(tipoDocumento)
                            .moeda(moeda)
                            .build();
                    transacoes.add(transacao);
                }
            }

            return TransacaoAIDTO.builder()
                    .banco(banco)
                    .moeda(moeda)
                    .tipoDocumento(tipoDocumento)
                    .totalTransacoes(transacoes.size())
                    .transacoes(transacoes)
                    .build();

        } catch (Exception e) {
            log.error("Erro ao processar resposta do Gemini: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao processar resposta do Gemini", e);
        }
    }
}
