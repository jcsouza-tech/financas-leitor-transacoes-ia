package br.com.financas.leitor_transacoes_ia.client;

import br.com.financas.leitor_transacoes_ia.model.dto.TransacaoAIDTO;
import br.com.financas.leitor_transacoes_ia.model.dto.TransacaoItemDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
@RequiredArgsConstructor
@Slf4j
public class OpenAIClientImpl implements AIClient {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.model:gpt-4}")
    private String model;

    @Value("${ai.timeout:30000}")
    private int timeout;

    @Value("${ai.max-tokens:4000}")
    private int maxTokens;

    @Override
    public TransacaoAIDTO processarDocumento(String textoExtraido, String banco, String moeda, String tipoDocumento) {
        log.info("Processando documento com OpenAI. Banco: {}, Moeda: {}, Tipo: {}, Tamanho texto: {}", 
                banco, moeda, tipoDocumento, textoExtraido.length());

        try {
            String prompt = construirPrompt(textoExtraido, banco, moeda, tipoDocumento);
            String resposta = chamarOpenAI(prompt);
            return processarRespostaOpenAI(resposta, banco, moeda, tipoDocumento);
        } catch (Exception e) {
            log.error("Erro ao processar documento com OpenAI: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao processar documento com OpenAI", e);
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

    private String chamarOpenAI(String prompt) {
        WebClient webClient = webClientBuilder
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "max_tokens", maxTokens,
                "temperature", 0.1
        );

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(java.time.Duration.ofMillis(timeout))
                .block();
    }

    private TransacaoAIDTO processarRespostaOpenAI(String resposta, String banco, String moeda, String tipoDocumento) {
        try {
            JsonNode jsonNode = objectMapper.readTree(resposta);
            String content = jsonNode.path("choices").get(0).path("message").path("content").asText();
            
            // Limpar o conteúdo para extrair apenas o JSON
            content = content.trim();
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
            log.error("Erro ao processar resposta do OpenAI: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao processar resposta do OpenAI", e);
        }
    }
}
