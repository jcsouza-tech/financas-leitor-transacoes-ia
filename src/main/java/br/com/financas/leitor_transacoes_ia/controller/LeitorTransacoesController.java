package br.com.financas.leitor_transacoes_ia.controller;

import br.com.financas.leitor_transacoes_ia.model.dto.ArquivoUploadDTO;
import br.com.financas.leitor_transacoes_ia.model.dto.TransacaoAIDTO;
import br.com.financas.leitor_transacoes_ia.model.dto.TransacaoDTO;
import br.com.financas.leitor_transacoes_ia.model.dto.ProcessamentoDTO;
import br.com.financas.leitor_transacoes_ia.model.dto.TransacaoRepresentationAssembler;
import br.com.financas.leitor_transacoes_ia.model.dto.ProcessamentoRepresentationAssembler;
import br.com.financas.leitor_transacoes_ia.model.entity.Transacao;
import br.com.financas.leitor_transacoes_ia.model.entity.Processamento;
import br.com.financas.leitor_transacoes_ia.repository.TransacaoRepository;
import br.com.financas.leitor_transacoes_ia.service.AIClassificadorService;
import br.com.financas.leitor_transacoes_ia.service.ExtratorTextoService;
import br.com.financas.leitor_transacoes_ia.service.PublicadorSQSService;
import br.com.financas.leitor_transacoes_ia.service.ProcessamentoService;
import org.springframework.hateoas.CollectionModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leitor")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Leitor de Transações IA", description = "API para processamento inteligente de documentos financeiros")
public class LeitorTransacoesController {
    
    private final ExtratorTextoService extratorTextoService;
    private final AIClassificadorService aiClassificadorService;
    private final PublicadorSQSService publicadorSQSService;
    private final TransacaoRepository transacaoRepository;
    private final ProcessamentoService processamentoService;
    private final TransacaoRepresentationAssembler transacaoAssembler;
    private final ProcessamentoRepresentationAssembler processamentoAssembler;
    
    @Value("${upload.max-file-size:52428800}") // 50MB
    private long maxFileSize;
    
    /**
     * Processa um arquivo PDF ou CSV usando IA para extrair e classificar transações
     */
    @PostMapping(value = "/processar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Processar documento financeiro",
        description = "Upload e processamento de arquivos PDF/CSV para extração e classificação de transações usando IA"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Documento processado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Arquivo inválido ou não suportado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> processarDocumento(
            @Parameter(description = "Arquivo PDF ou CSV para processamento")
            @RequestParam("arquivo") MultipartFile arquivo,
            
            @Parameter(description = "Nome do banco ou instituição financeira")
            @RequestParam("banco") String banco,
            
            @Parameter(description = "Moeda do documento (BRL, USD, EUR, etc.)")
            @RequestParam("moeda") String moeda,
            
            @Parameter(description = "Tipo do documento: EXTRATO ou FATURA_CARTAO")
            @RequestParam("tipo") String tipoDocumento) {
        
        String requestId = UUID.randomUUID().toString();
        log.info("Iniciando processamento. Request ID: {}, Arquivo: {}, Banco: {}, Moeda: {}, Tipo: {}", 
                requestId, arquivo.getOriginalFilename(), banco, moeda, tipoDocumento);
        
        try {
            // Validações
            if (arquivo.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Arquivo não pode estar vazio"));
            }
            
            if (!extratorTextoService.isArquivoSuportado(arquivo)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Tipo de arquivo não suportado. Use PDF ou CSV"));
            }
            
            if (!extratorTextoService.isTamanhoValido(arquivo, maxFileSize)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Arquivo muito grande. Máximo: " + (maxFileSize / 1024 / 1024) + "MB"));
            }
            
            // Extrair texto
            String textoExtraido = extratorTextoService.extrairTexto(arquivo);
            
            // Criar processamento
            Processamento processamento = processamentoService.criarProcessamento(
                arquivo.getOriginalFilename(), banco, moeda, tipoDocumento);

            // Classificar com IA
            TransacaoAIDTO transacoesClassificadas = aiClassificadorService.processarDocumento(textoExtraido, banco, moeda, tipoDocumento);
            
            // Incluir ID do processamento
            transacoesClassificadas.setProcessamentoId(processamento.getProcessamentoId());

            // Publicar no SQS
            publicadorSQSService.publicarTransacoes(transacoesClassificadas, banco, tipoDocumento);
            
            // Resposta de sucesso
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("mensagem", "Documento processado com sucesso");
            resposta.put("requestId", requestId);
            resposta.put("totalTransacoes", transacoesClassificadas.getTotalTransacoes());
            resposta.put("banco", transacoesClassificadas.getBanco());
            resposta.put("moeda", transacoesClassificadas.getMoeda());
            resposta.put("tipoDocumento", transacoesClassificadas.getTipoDocumento());
            resposta.put("status", "PROCESSADO");
            
            log.info("Processamento concluído com sucesso. Request ID: {}, Transações: {}", 
                    requestId, transacoesClassificadas.getTotalTransacoes());
            
            return ResponseEntity.ok(resposta);
            
        } catch (Exception e) {
            log.error("Erro ao processar documento. Request ID: {}, Erro: {}", requestId, e.getMessage(), e);
            
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro interno do servidor");
            erro.put("requestId", requestId);
            erro.put("status", "ERRO");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }
    
    /**
     * Consulta o status de um processamento
     */
    @GetMapping("/status/{requestId}")
    @Operation(
        summary = "Consultar status do processamento",
        description = "Consulta o status de um processamento pelo ID da requisição"
    )
    public ResponseEntity<Map<String, Object>> consultarStatus(
            @Parameter(description = "ID da requisição")
            @PathVariable String requestId) {
        
        // Implementação placeholder - em produção seria consultado um cache ou banco
        Map<String, Object> status = new HashMap<>();
        status.put("requestId", requestId);
        status.put("status", "PROCESSADO");
        status.put("mensagem", "Processamento concluído");
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Lista todas as transações salvas no banco de dados
     */
    @GetMapping("/transacoes")
    @Operation(
        summary = "Listar todas as transações",
        description = "Retorna todas as transações salvas no banco de dados com links HATEOAS"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de transações retornada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<CollectionModel<TransacaoDTO>> listarTransacoes() {
        try {
            log.info("Listando todas as transações");
            
            var transacoes = transacaoRepository.findAll();
            CollectionModel<TransacaoDTO> transacoesDTO = transacaoAssembler.toCollectionModel(transacoes);
            
            log.info("Listagem concluída. Total de transações: {}", transacoes.size());
            
            return ResponseEntity.ok(transacoesDTO);
            
        } catch (Exception e) {
            log.error("Erro ao listar transações: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Lista transações por banco
     */
    @GetMapping("/transacoes/banco/{banco}")
    @Operation(
        summary = "Listar transações por banco",
        description = "Retorna transações filtradas por banco"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de transações retornada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> listarTransacoesPorBanco(
            @Parameter(description = "Nome do banco")
            @PathVariable String banco) {
        try {
            log.info("Listando transações do banco: {}", banco);
            
            var transacoes = transacaoRepository.findByBanco(banco);
            
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("banco", banco);
            resposta.put("total", transacoes.size());
            resposta.put("transacoes", transacoes);
            resposta.put("status", "SUCESSO");
            
            log.info("Listagem concluída. Total de transações do banco {}: {}", banco, transacoes.size());
            
            return ResponseEntity.ok(resposta);
            
        } catch (Exception e) {
            log.error("Erro ao listar transações do banco {}: {}", banco, e.getMessage(), e);
            
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro interno do servidor");
            erro.put("status", "ERRO");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }
    
    /**
     * Lista transações por período
     */
    @GetMapping("/transacoes/periodo")
    @Operation(
        summary = "Listar transações por período",
        description = "Retorna transações filtradas por período (formato: yyyy-MM-dd)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de transações retornada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetros de data inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> listarTransacoesPorPeriodo(
            @Parameter(description = "Data de início (yyyy-MM-dd)")
            @RequestParam String dataInicio,
            
            @Parameter(description = "Data de fim (yyyy-MM-dd)")
            @RequestParam String dataFim) {
        try {
            log.info("Listando transações do período: {} a {}", dataInicio, dataFim);
            
            var inicio = LocalDate.parse(dataInicio);
            var fim = LocalDate.parse(dataFim);
            
            var transacoes = transacaoRepository.findByDataBetween(inicio, fim);
            
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("periodo", Map.of("inicio", dataInicio, "fim", dataFim));
            resposta.put("total", transacoes.size());
            resposta.put("transacoes", transacoes);
            resposta.put("status", "SUCESSO");
            
            log.info("Listagem concluída. Total de transações no período: {}", transacoes.size());
            
            return ResponseEntity.ok(resposta);
            
        } catch (Exception e) {
            log.error("Erro ao listar transações do período {} a {}: {}", dataInicio, dataFim, e.getMessage(), e);
            
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro ao processar parâmetros de data");
            erro.put("status", "ERRO");
            
            return ResponseEntity.badRequest().body(erro);
        }
    }

    /**
     * Lista todos os processamentos
     */
    @GetMapping("/processamentos")
    @Operation(
        summary = "Listar todos os processamentos",
        description = "Retorna todos os processamentos de documentos com links HATEOAS"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de processamentos retornada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<CollectionModel<ProcessamentoDTO>> listarProcessamentos() {
        try {
            log.info("Listando todos os processamentos");

            var processamentos = processamentoService.listarProcessamentos();
            CollectionModel<ProcessamentoDTO> processamentosDTO = processamentoAssembler.toCollectionModel(processamentos);

            log.info("Listagem concluída. Total de processamentos: {}", processamentos.size());

            return ResponseEntity.ok(processamentosDTO);

        } catch (Exception e) {
            log.error("Erro ao listar processamentos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Consulta o status de um processamento
     */
    @GetMapping("/processamentos/{processamentoId}")
    @Operation(
        summary = "Consultar status do processamento",
        description = "Consulta o status de um processamento pelo ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status do processamento retornado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Processamento não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<ProcessamentoDTO> consultarStatusProcessamento(
            @Parameter(description = "ID do processamento")
            @PathVariable String processamentoId) {
        try {
            log.info("Consultando status do processamento: {}", processamentoId);

            var processamento = processamentoService.buscarPorId(processamentoId);

            if (processamento.isPresent()) {
                ProcessamentoDTO processamentoDTO = processamentoAssembler.toModel(processamento.get());
                return ResponseEntity.ok(processamentoDTO);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Erro ao consultar status do processamento {}: {}", processamentoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cancela um processamento
     */
    @PostMapping("/processamentos/{processamentoId}/cancelar")
    @Operation(
        summary = "Cancelar processamento",
        description = "Cancela um processamento pendente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Processamento cancelado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Processamento não pode ser cancelado"),
        @ApiResponse(responseCode = "404", description = "Processamento não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, Object>> cancelarProcessamento(
            @Parameter(description = "ID do processamento")
            @PathVariable String processamentoId) {
        try {
            log.info("Cancelando processamento: {}", processamentoId);

            var processamento = processamentoService.cancelarProcessamento(processamentoId);

            Map<String, Object> resposta = new HashMap<>();
            resposta.put("mensagem", "Processamento cancelado com sucesso");
            resposta.put("processamento", processamento);
            resposta.put("status", "SUCESSO");

            log.info("Processamento cancelado com sucesso: {}", processamentoId);

            return ResponseEntity.ok(resposta);

        } catch (RuntimeException e) {
            log.error("Erro ao cancelar processamento {}: {}", processamentoId, e.getMessage());

            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("status", "ERRO");

            return ResponseEntity.badRequest().body(erro);

        } catch (Exception e) {
            log.error("Erro interno ao cancelar processamento {}: {}", processamentoId, e.getMessage(), e);

            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro interno do servidor");
            erro.put("status", "ERRO");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
        }
    }
}
