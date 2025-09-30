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

@Path("/chefs")
public class ChefResource {

    @Context
    UriInfo uriInfo;

    private ChefRepresentation rep(Chef c) {
        return ChefRepresentation.from(c, uriInfo);
    }

    private List<ChefRepresentation> repList(List<Chef> chefs) {
        return chefs.stream().map(this::rep).collect(Collectors.toList());
    }

    @GET
    @Operation(summary = "Retorna todos os chefs")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChefRepresentation.class, type = SchemaType.ARRAY)))
    public Response getAll() {
        return Response.ok(repList(Chef.listAll())).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Retorna um chef específico pelo ID")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChefRepresentation.class)))
    @APIResponse(responseCode = "404", description = "Chef não encontrado")
    public Response getById(@PathParam("id") long id) {
        Chef entity = Chef.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }
        return Response.ok(rep(entity)).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Busca chefs por nome ou especialidade com paginação")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SearchChefResponse.class)))
    public Response search(
            @QueryParam("q") String q,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        Set<String> allowed = Set.of("id", "nome", "especialidade", "anosDeExperiencia");
        if (!allowed.contains(sort)) sort = "id";

        Sort sortObj = Sort.by(sort, "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending);
        int effectivePage = page <= 1 ? 0 : page - 1;

        PanacheQuery<Chef> query = (q == null || q.isBlank())
                ? Chef.findAll(sortObj)
                : Chef.find("lower(nome) like ?1 or lower(especialidade) like ?1", sortObj, "%" + q.toLowerCase() + "%");

        long totalElements = query.count();
        long totalPages = (long) Math.ceil((double) totalElements / size);
        List<Chef> chefs = query.page(effectivePage, size).list();

        SearchChefResponse response = SearchChefResponse.from(chefs, uriInfo, q, sort, direction, page, size, totalElements, totalPages);
        return Response.ok(response).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Cria um novo chef")
    @APIResponse(responseCode = "201", description = "Criado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChefRepresentation.class)))
    public Response insert(@Valid Chef chef) {
        Chef.persist(chef);
        return Response.status(201).entity(rep(chef)).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    @Operation(summary = "Exclui um chef")
    @APIResponse(responseCode = "204", description = "Excluído")
    @APIResponse(responseCode = "404", description = "Chef não encontrado")
    public Response delete(@PathParam("id") long id) {
        if (Chef.deleteById(id)) {
            return Response.noContent().build();
        }
        return Response.status(404).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    @Operation(summary = "Atualiza um chef")
    @APIResponse(responseCode = "200", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChefRepresentation.class)))
    @APIResponse(responseCode = "404", description = "Chef não encontrado")
    public Response update(@PathParam("id") long id, @Valid Chef newChef) {
        Chef entity = Chef.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }
        entity.nome = newChef.nome;
        entity.especialidade = newChef.especialidade;
        entity.anosDeExperiencia = newChef.anosDeExperiencia;
        return Response.ok(rep(entity)).build();
    }

    // --- Endpoints de Relacionamento ---

    @PUT
    @Path("{id}/prato-assinado/{pratoId}")
    @Transactional
    @Operation(summary = "Atribui um prato de assinatura a um chef")
    @APIResponse(responseCode = "200", description = "Prato atribuído")
    @APIResponse(responseCode = "404", description = "Chef ou Prato não encontrado")
    public Response assignSignatureDish(@PathParam("id") long chefId, @PathParam("pratoId") long pratoId) {
        Chef chef = Chef.findById(chefId);
        if (chef == null) return Response.status(404).entity("Chef não encontrado").build();

        Prato prato = Prato.findById(pratoId);
        if (prato == null) return Response.status(404).entity("Prato não encontrado").build();

        chef.pratoAssinado = prato;
        return Response.ok(rep(chef)).build();
    }
}