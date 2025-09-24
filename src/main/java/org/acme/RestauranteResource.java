package org.acme;


import org.acme.RestauranteRepresentation;
import org.acme.SearchRestauranteResponse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/restaurantes")
public class RestauranteResource {

    @Context
    UriInfo uriInfo;


    private RestauranteRepresentation rep(Restaurante r) {
        return RestauranteRepresentation.from(r, uriInfo);
    }

    private List<RestauranteRepresentation> repList(List<Restaurante> restaurantes) {
        return restaurantes.stream().map(this::rep).collect(Collectors.toList());
    }

    @GET
    @Operation(
            summary = "Retorna todos os restaurantes",
            description = "Retorna uma lista de restaurantes por padrão no formato JSON."
    )
    @APIResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RestauranteRepresentation.class, type = SchemaType.ARRAY)
            )
    )
    public Response getAll() {
        List<Restaurante> restaurantes = Restaurante.listAll();
        return Response.ok(repList(restaurantes)).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Busca um restaurante pelo ID")
    public Response getById(
            @Parameter(description = "Id do restaurante a ser pesquisado", required = true)
            @PathParam("id") long id) {
        Restaurante entity = Restaurante.findById(id);
        if (entity == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(rep(entity)).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Busca, ordena e pagina restaurantes")
    public Response search(
            @Parameter(description = "Query de busca por nome ou endereço")
            @QueryParam("q") String q,
            @Parameter(description = "Campo de ordenação da lista de retorno")
            @QueryParam("sort") @DefaultValue("id") String sort,
            @Parameter(description = "Direção da ordenação (asc/desc)")
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @Parameter(description = "Número da página")
            @QueryParam("page") @DefaultValue("1") int page,
            @Parameter(description = "Quantidade de itens por página")
            @QueryParam("size") @DefaultValue("10") int size) {


        Set<String> allowedSortFields = Set.of("id", "nome", "endereco");
        if (!allowedSortFields.contains(sort)) {
            sort = "id";
        }

        Sort sortObj = Sort.by(
                sort,
                "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending
        );

        int effectivePage = page <= 1 ? 0 : page - 1;


        PanacheQuery<Restaurante> query = (q == null || q.isBlank())
                ? Restaurante.findAll(sortObj)
                : Restaurante.find("lower(nome) like ?1 or lower(endereco) like ?1",
                sortObj,
                "%" + q.toLowerCase() + "%");

        long totalElements = query.count();
        long totalPages = (long) Math.ceil((double) totalElements / size);

        List<Restaurante> restaurantes = query.page(effectivePage, size).list();

        SearchRestauranteResponse response = SearchRestauranteResponse.from(
                restaurantes, uriInfo, q, sort, direction, page, size, totalElements, totalPages
        );

        return Response.ok(response).build();
    }

    @POST
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Restaurante.class)
            )
    )
    @APIResponse(responseCode = "201", description = "Criado")
    @APIResponse(responseCode = "400", description = "Requisição Inválida")
    @Transactional
    public Response insert(Restaurante restaurante) {
        Restaurante.persist(restaurante);
        return Response.status(Response.Status.CREATED).entity(rep(restaurante)).build();
    }

    @DELETE
    @Transactional
    @Path("{id}")
    @Operation(summary = "Deleta um restaurante pelo ID")
    public Response delete(@PathParam("id") long id) {
        boolean deleted = Restaurante.deleteById(id);
        return deleted ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Transactional
    @Path("{id}")
    @Operation(summary = "Atualiza um restaurante pelo ID")
    public Response update(@PathParam("id") long id, Restaurante restauranteAtualizado) {
        Restaurante entity = Restaurante.findById(id);
        if (entity == null)
            return Response.status(Response.Status.NOT_FOUND).build();


        entity.nome = restauranteAtualizado.nome;
        entity.endereco = restauranteAtualizado.endereco;

        return Response.ok(rep(entity)).build();
    }
}