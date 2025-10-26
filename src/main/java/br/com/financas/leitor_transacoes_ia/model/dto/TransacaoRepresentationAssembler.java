package br.com.financas.leitor_transacoes_ia.model.dto;

import br.com.financas.leitor_transacoes_ia.controller.LeitorTransacoesController;
import br.com.financas.leitor_transacoes_ia.model.entity.Transacao;
import org.jetbrains.annotations.NotNull;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Classe para tratar a conversão de Transacao para TransacaoDTO implementando o HATEOAS
 */
@Component
public class TransacaoRepresentationAssembler implements RepresentationModelAssembler<Transacao, TransacaoDTO> {
    
    /**
     * Converte uma entidade Transacao e retorna uma TransacaoDTO com links HATEOAS
     * @param entity entidade Transacao
     * @return bean TransacaoDTO com links
     */
    @NotNull
    @Override
    public TransacaoDTO toModel(Transacao entity) {
        TransacaoDTO dto = TransacaoDTO.builder()
                .id(entity.getId())
                .data(entity.getData())
                .lancamento(entity.getLancamento())
                .detalhes(entity.getDetalhes())
                .numeroDocumento(entity.getNumeroDocumento())
                .valor(entity.getValor())
                .moeda(entity.getMoeda())
                .tipoLancamento(entity.getTipoLancamento())
                .categoria(entity.getCategoria())
                .banco(entity.getBanco())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
        
        // Adiciona links HATEOAS
        dto.add(linkTo(methodOn(LeitorTransacoesController.class).listarTransacoes()).withRel("transacoes"));
        dto.add(linkTo(methodOn(LeitorTransacoesController.class).listarTransacoesPorBanco(entity.getBanco())).withRel("transacoes-banco"));
        dto.add(linkTo(methodOn(LeitorTransacoesController.class).listarProcessamentos()).withRel("processamentos"));
        
        return dto;
    }

    /**
     * Conversão de uma coleção de Transacoes para CollectionModel com HATEOAS
     * @param entities coleção de entidades
     * @return CollectionModel<TransacaoDTO>
     */
    @NotNull
    @Override
    public CollectionModel<TransacaoDTO> toCollectionModel(@NotNull Iterable<? extends Transacao> entities) {
        CollectionModel<TransacaoDTO> collectionModel = RepresentationModelAssembler.super.toCollectionModel(entities);
        
        // Adiciona links para a coleção
        collectionModel.add(linkTo(methodOn(LeitorTransacoesController.class).listarTransacoes()).withSelfRel());
        collectionModel.add(linkTo(methodOn(LeitorTransacoesController.class).listarProcessamentos()).withRel("processamentos"));
        
        return collectionModel;
    }
}