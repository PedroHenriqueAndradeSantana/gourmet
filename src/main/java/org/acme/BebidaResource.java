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

@Path("/bebidas")
public class BebidaResource {

    @Context
    UriInfo uriInfo;

    private BebidaRepresentation rep(Bebida b) {
        return BebidaRepresentation.from(b, uriInfo);
    }

    private List<BebidaRepresentation> repList(List<Bebida> bebidas) {
        return bebidas.stream().map(this::rep).collect(Collectors.toList());
    }

    @GET
    @Operation(summary = "Retorna todas as bebidas")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BebidaRepresentation.class, type = SchemaType.ARRAY)))
    public Response getAll() {
        return Response.ok(repList(Bebida.listAll())).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Retorna uma bebida específica pelo ID")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BebidaRepresentation.class)))
    @APIResponse(responseCode = "404", description = "Bebida não encontrada")
    public Response getById(@PathParam("id") long id) {
        Bebida entity = Bebida.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }
        return Response.ok(rep(entity)).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Busca bebidas por nome com paginação")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SearchBebidaResponse.class)))
    public Response search(
            @QueryParam("q") String q,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        Set<String> allowed = Set.of("id", "nome", "preco", "volumeMl");
        if (!allowed.contains(sort)) sort = "id";

        Sort sortObj = Sort.by(sort, "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        int effectivePage = page <= 1 ? 0 : page - 1;

        PanacheQuery<Bebida> query = (q == null || q.isBlank())
                ? Bebida.findAll(sortObj)
                : Bebida.find("lower(nome) like ?1", sortObj, "%" + q.toLowerCase() + "%");

        long totalElements = query.count();
        long totalPages = (long) Math.ceil((double) totalElements / size);
        List<Bebida> bebidas = query.page(effectivePage, size).list();

        SearchBebidaResponse response = SearchBebidaResponse.from(bebidas, uriInfo, q, sort, direction, page, size, totalElements, totalPages);
        return Response.ok(response).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Cria uma nova bebida")
    @APIResponse(responseCode = "201", description = "Criada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BebidaRepresentation.class)))
    public Response insert(@Valid Bebida bebida) {
        Bebida.persist(bebida);
        return Response.status(201).entity(rep(bebida)).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    @Operation(summary = "Exclui uma bebida")
    @APIResponse(responseCode = "204", description = "Excluída")
    @APIResponse(responseCode = "404", description = "Bebida não encontrada")
    public Response delete(@PathParam("id") long id) {
        if (Bebida.deleteById(id)) {
            return Response.noContent().build();
        }
        return Response.status(404).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    @Operation(summary = "Atualiza uma bebida")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BebidaRepresentation.class)))
    @APIResponse(responseCode = "404", description = "Bebida não encontrada")
    public Response update(@PathParam("id") long id, @Valid Bebida newBebida) {
        Bebida entity = Bebida.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }
        entity.nome = newBebida.nome;
        entity.preco = newBebida.preco;
        entity.volumeMl = newBebida.volumeMl;
        entity.alcoolica = newBebida.alcoolica;
        return Response.ok(rep(entity)).build();
    }




}