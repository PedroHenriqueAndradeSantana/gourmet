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

@Path("/pedidos")
public class PedidoResource {

    @Context
    UriInfo uriInfo;

    private PedidoRepresentation rep(Pedido p) {
        return PedidoRepresentation.from(p, uriInfo);
    }

    private List<PedidoRepresentation> repList(List<Pedido> pedidos) {
        return pedidos.stream().map(this::rep).collect(Collectors.toList());
    }

    @GET
    @Operation(summary = "Retorna todos os pedidos")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PedidoRepresentation.class, type = SchemaType.ARRAY)))
    public Response getAll() {
        return Response.ok(repList(Pedido.listAll())).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Retorna um pedido específico pelo ID")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PedidoRepresentation.class)))
    @APIResponse(responseCode = "404", description = "Pedido não encontrado")
    public Response getById(@PathParam("id") long id) {
        Pedido entity = Pedido.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }
        return Response.ok(rep(entity)).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Busca pedidos por nome do cliente com paginação")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SearchPedidoResponse.class)))
    public Response search(
            @QueryParam("q") String q,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        Set<String> allowed = Set.of("id", "nomeCliente", "numeroMesa", "status");
        if (!allowed.contains(sort)) sort = "id";

        Sort sortObj = Sort.by(sort, "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        int effectivePage = page <= 1 ? 0 : page - 1;

        PanacheQuery<Pedido> query = (q == null || q.isBlank())
                ? Pedido.findAll(sortObj)
                : Pedido.find("lower(nomeCliente) like ?1", sortObj, "%" + q.toLowerCase() + "%");

        long totalElements = query.count();
        long totalPages = (long) Math.ceil((double) totalElements / size);
        List<Pedido> pedidos = query.page(effectivePage, size).list();

        SearchPedidoResponse response = SearchPedidoResponse.from(pedidos, uriInfo, q, sort, direction, page, size, totalElements, totalPages);
        return Response.ok(response).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Cria um novo pedido")
    @APIResponse(responseCode = "201", description = "Criado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PedidoRepresentation.class)))
    public Response insert(@Valid Pedido pedido) {
        Pedido.persist(pedido);
        return Response.status(201).entity(rep(pedido)).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    @Operation(summary = "Exclui um pedido")
    @APIResponse(responseCode = "204", description = "Excluído")
    @APIResponse(responseCode = "404", description = "Pedido não encontrado")
    public Response delete(@PathParam("id") long id) {
        if (Pedido.deleteById(id)) {
            return Response.noContent().build();
        }
        return Response.status(404).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    @Operation(summary = "Atualiza um pedido")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PedidoRepresentation.class)))
    @APIResponse(responseCode = "404", description = "Pedido não encontrado")
    public Response update(@PathParam("id") long id, @Valid Pedido newPedido) {
        Pedido entity = Pedido.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }
        entity.nomeCliente = newPedido.nomeCliente;
        entity.numeroMesa = newPedido.numeroMesa;
        entity.status = newPedido.status;
        return Response.ok(rep(entity)).build();
    }
    
    // --- Endpoints de Relacionamento (Adicionar/Remover Pratos) ---

    @POST
    @Path("{id}/pratos/{pratoId}")
    @Transactional
    @Operation(summary = "Adiciona um prato a um pedido")
    @APIResponse(responseCode = "200", description = "Prato adicionado")
    @APIResponse(responseCode = "404", description = "Pedido ou Prato não encontrado")
    @APIResponse(responseCode = "409", description = "Prato já existe no pedido")
    public Response addPrato(@PathParam("id") long pedidoId, @PathParam("pratoId") long pratoId) {
        Pedido pedido = Pedido.findById(pedidoId);
        if (pedido == null) return Response.status(404).entity("Pedido não encontrado").build();

        Prato prato = Prato.findById(pratoId);
        if (prato == null) return Response.status(404).entity("Prato não encontrado").build();

        if (pedido.pratos.contains(prato)) {
            return Response.status(409).entity("Prato já existe neste pedido").build();
        }

        pedido.pratos.add(prato);
        return Response.ok(rep(pedido)).build();
    }

    @GET
    @Path("{id}/pratos")
    @Operation(summary = "Lista os pratos de um pedido")
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "404", description = "Pedido não encontrado")
    public Response listPratos(@PathParam("id") long pedidoId) {
        Pedido pedido = Pedido.findById(pedidoId);
        if (pedido == null) return Response.status(404).build();
        
        // Usamos a representação do Pedido, que já formata a lista de pratos
        return Response.ok(rep(pedido)).build();
    }

    @DELETE
    @Path("{id}/pratos/{pratoId}")
    @Transactional
    @Operation(summary = "Remove um prato de um pedido")
    @APIResponse(responseCode = "204", description = "Prato removido")
    @APIResponse(responseCode = "404", description = "Pedido ou Prato não encontrado, ou prato não pertence ao pedido")
    public Response removePrato(@PathParam("id") long pedidoId, @PathParam("pratoId") long pratoId) {
        Pedido pedido = Pedido.findById(pedidoId);
        if (pedido == null) return Response.status(404).entity("Pedido não encontrado").build();

        Prato prato = Prato.findById(pratoId);
        if (prato == null) return Response.status(404).entity("Prato não encontrado").build();

        if (!pedido.pratos.contains(prato)) {
            return Response.status(404).entity("Prato não pertence a este pedido").build();
        }

        pedido.pratos.remove(prato);
        return Response.noContent().build();
    }
}