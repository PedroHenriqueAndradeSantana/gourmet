package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/pratos")
public class PratoResource {

    @Context
    UriInfo uriInfo;

    private PratoRepresentation rep(Prato p) {
        return PratoRepresentation.from(p, uriInfo);
    }

    private List<PratoRepresentation> repList(List<Prato> pratos) {
        return pratos.stream().map(this::rep).collect(Collectors.toList());
    }

    @GET
    @Operation(summary = "Retorna todos os pratos")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PratoRepresentation.class, type = SchemaType.ARRAY)))
    public Response getAll() {
        return Response.ok(repList(Prato.listAll())).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Retorna um prato específico pelo ID")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PratoRepresentation.class)))
    @APIResponse(responseCode = "404", description = "Prato não encontrado")
    public Response getById(@PathParam("id") long id) {
        Prato entity = Prato.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }
        return Response.ok(rep(entity)).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Busca pratos por nome ou descrição com paginação")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SearchPratoResponse.class)))
    public Response search(
            @QueryParam("q") String q,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        Set<String> allowed = Set.of("id", "nome", "descricao", "tempoPreparoMinutos");
        if (!allowed.contains(sort)) sort = "id";

        Sort sortObj = Sort.by(sort, "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        int effectivePage = page <= 1 ? 0 : page - 1;

        PanacheQuery<Prato> query = (q == null || q.isBlank())
                ? Prato.findAll(sortObj)
                : Prato.find("lower(nome) like ?1 or lower(descricao) like ?1", sortObj, "%" + q.toLowerCase() + "%");

        long totalElements = query.count();
        long totalPages = (long) Math.ceil((double) totalElements / size);
        List<Prato> pratos = query.page(effectivePage, size).list();

        SearchPratoResponse response = SearchPratoResponse.from(pratos, uriInfo, q, sort, direction, page, size, totalElements, totalPages);
        return Response.ok(response).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Cria um novo prato")
    @APIResponse(responseCode = "201", description = "Criado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PratoRepresentation.class)))
    public Response insert(@Valid Prato prato) {
        Prato.persist(prato);
        return Response.status(201).entity(rep(prato)).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    @Operation(summary = "Exclui um prato")
    @APIResponse(responseCode = "204", description = "Excluído")
    @APIResponse(responseCode = "404", description = "Prato não encontrado")
    public Response delete(@PathParam("id") long id) {
        if (Prato.deleteById(id)) {
            return Response.noContent().build();
        }
        return Response.status(404).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    @Operation(summary = "Atualiza um prato")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PratoRepresentation.class)))
    @APIResponse(responseCode = "404", description = "Prato não encontrado")
    public Response update(@PathParam("id") long id, @Valid Prato newPrato) {
        Prato entity = Prato.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }
        entity.nome = newPrato.nome;
        entity.descricao = newPrato.descricao;
        entity.tempoPreparoMinutos = newPrato.tempoPreparoMinutos;
        entity.chefResponsavel = newPrato.chefResponsavel;
        return Response.ok(rep(entity)).build();
    }
    
    // --- Endpoints de Relacionamento ---

    @PUT
    @Path("{id}/chef/{chefId}")
    @Transactional
    @Operation(summary = "Atribui um chef a um prato")
    @APIResponse(responseCode = "200", description = "Chef atribuído")
    @APIResponse(responseCode = "404", description = "Prato ou Chef não encontrado")
    public Response assignChef(@PathParam("id") long pratoId, @PathParam("chefId") long chefId) {
        Prato prato = Prato.findById(pratoId);
        if (prato == null) return Response.status(404).entity("Prato não encontrado").build();

        Chef chef = Chef.findById(chefId);
        if (chef == null) return Response.status(404).entity("Chef não encontrado").build();

        prato.chefResponsavel = chef;
        return Response.ok(rep(prato)).build();
    }

    @GET
    @Path("{id}/pedidos")
    @Operation(summary = "Lista todos os pedidos que contêm um prato")
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "404", description = "Prato não encontrado")
    public Response listPedidos(@PathParam("id") long pratoId) {
        Prato prato = Prato.findById(pratoId);
        if (prato == null) return Response.status(404).build();
        
        // Esta parte pode ser otimizada, mas para o exemplo, listamos os pedidos.
        // Em um cenário real, você poderia criar um PedidoRepresentation simplificado.
        return Response.ok(prato.pedidos).build();
    }
}