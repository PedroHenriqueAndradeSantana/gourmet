package org.acme;


import org.acme.Chef; // LINHA CORRETA

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.List;

@Path("/chefs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Chefs", description = "Operações para gerenciamento de chefs de cozinha")
public class ChefResource {

    @GET
    @Operation(summary = "Listar todos os chefs")
    public Response getAll() {
        return Response.ok(Chef.listAll()).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Buscar chef por ID")
    public Response getById(@PathParam("id") Long id) {
        return Chef.findByIdOptional(id)
                .map(chef -> Response.ok(chef).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Transactional
    @Operation(summary = "Criar um novo chef")
    public Response create(Chef chef) {
        chef.persist();
        return Response.status(Response.Status.CREATED).entity(chef).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Atualizar um chef existente")
    public Response update(@PathParam("id") Long id, Chef chefAtualizado) {
        return Chef.<Chef>findByIdOptional(id)
                .map(chef -> {
                    chef.nome = chefAtualizado.nome;
                    chef.especialidade = chefAtualizado.especialidade;
                    return Response.ok(chef).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Deletar um chef")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = Chef.deleteById(id);
        return deleted ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    // --- Endpoint com Consulta Personalizada ---
    @GET
    @Path("/buscar/por-especialidade")
    @Operation(summary = "Buscar chefs por especialidade")
    public Response findByEspecialidade(@Parameter(description = "Especialidade a ser buscada") @QueryParam("q") String q) {
        List<Chef> chefs = Chef.list("lower(especialidade) like ?1", "%" + q.toLowerCase() + "%");
        return Response.ok(chefs).build();
    }
}