package br.com.financas.leitor_transacoes_ia.model.dto;

import br.com.financas.leitor_transacoes_ia.controller.LeitorTransacoesController;
import br.com.financas.leitor_transacoes_ia.model.entity.Processamento;
import org.jetbrains.annotations.NotNull;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Classe para tratar a conversão de Processamento para ProcessamentoDTO implementando o HATEOAS
 */
@Component
public class ProcessamentoRepresentationAssembler implements RepresentationModelAssembler<Processamento, ProcessamentoDTO> {
    
    /**
     * Converte uma entidade Processamento e retorna uma ProcessamentoDTO com links HATEOAS
     * @param entity entidade Processamento
     * @return bean ProcessamentoDTO com links
     */
    @NotNull
    @Override
    public ProcessamentoDTO toModel(Processamento entity) {
        ProcessamentoDTO dto = ProcessamentoDTO.builder()
                .id(entity.getId())
                .processamentoId(entity.getProcessamentoId())
                .nomeArquivo(entity.getNomeArquivo())
                .banco(entity.getBanco())
                .moeda(entity.getMoeda())
                .tipoDocumento(entity.getTipoDocumento())
                .status(entity.getStatus())
                .progresso(entity.getProgresso())
                .dataInicio(entity.getDataInicio())
                .dataFim(entity.getDataFim())
                .transacoesProcessadas(entity.getTransacoesProcessadas())
                .transacoesSalvas(entity.getTransacoesSalvas())
                .duplicatasIgnoradas(entity.getDuplicatasIgnoradas())
                .tempoProcessamentoMs(entity.getTempoProcessamentoMs())
                .velocidadeProcessamento(entity.getVelocidadeProcessamento())
                .mensagem(entity.getMensagem())
                .erro(entity.getErro())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
        
        // Adiciona links HATEOAS
        dto.add(linkTo(methodOn(LeitorTransacoesController.class).consultarStatusProcessamento(entity.getProcessamentoId())).withSelfRel());
        dto.add(linkTo(methodOn(LeitorTransacoesController.class).listarProcessamentos()).withRel("processamentos"));
        dto.add(linkTo(methodOn(LeitorTransacoesController.class).listarTransacoes()).withRel("transacoes"));
        
        // Link para cancelar se estiver pendente
        if (entity.getStatus() == Processamento.StatusProcessamento.PENDENTE) {
            dto.add(linkTo(methodOn(LeitorTransacoesController.class).cancelarProcessamento(entity.getProcessamentoId())).withRel("cancelar"));
        }
        
        return dto;
    }

    /**
     * Conversão de uma coleção de Processamentos para CollectionModel com HATEOAS
     * @param entities coleção de entidades
     * @return CollectionModel<ProcessamentoDTO>
     */
    @NotNull
    @Override
    public CollectionModel<ProcessamentoDTO> toCollectionModel(@NotNull Iterable<? extends Processamento> entities) {
        CollectionModel<ProcessamentoDTO> collectionModel = RepresentationModelAssembler.super.toCollectionModel(entities);
        
        // Adiciona links para a coleção
        collectionModel.add(linkTo(methodOn(LeitorTransacoesController.class).listarProcessamentos()).withSelfRel());
        collectionModel.add(linkTo(methodOn(LeitorTransacoesController.class).listarTransacoes()).withRel("transacoes"));
        
        return collectionModel;
    }
}