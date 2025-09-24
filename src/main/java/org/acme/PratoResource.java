package org.acme;


import org.acme.CategoriaPrato; // LINHA CORRETA
import org.acme.Prato;           // LINHA CORRETA
import org.acme.Restaurante;    // LINHA CORRETA

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;

@Path("/pratos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Pratos", description = "Operações para gerenciamento de pratos do cardápio")
public class PratoResource {

    @GET
    @Operation(summary = "Listar todos os pratos")
    public Response getAll() {
        return Response.ok(Prato.listAll()).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Buscar prato por ID")
    public Response getById(@PathParam("id") Long id) {
        return Prato.findByIdOptional(id)
                .map(prato -> Response.ok(prato).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }


    @POST
    @Path("/restaurante/{restauranteId}")
    @Transactional
    @Operation(summary = "Criar um novo prato para um restaurante")
    public Response create(@PathParam("restauranteId") Long restauranteId, Prato prato) {
        Restaurante restaurante = Restaurante.findById(restauranteId);
        if (restaurante == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Restaurante não encontrado").build();
        }
        prato.restaurante = restaurante;
        prato.persist();
        return Response.status(Response.Status.CREATED).entity(prato).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Atualizar um prato existente")
    public Response update(@PathParam("id") Long id, Prato pratoAtualizado) {
        return Prato.<Prato>findByIdOptional(id)
                .map(prato -> {
                    prato.nome = pratoAtualizado.nome;
                    prato.preco = pratoAtualizado.preco;
                    prato.categoria = pratoAtualizado.categoria;
                    prato.ingredientes = pratoAtualizado.ingredientes;
                    return Response.ok(prato).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Deletar um prato")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = Prato.deleteById(id);
        return deleted ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }


    @GET
    @Path("/buscar/por-categoria")
    @Operation(summary = "Buscar pratos por categoria")
    public Response findByCategoria(@Parameter(description = "Categoria do prato") @QueryParam("c") CategoriaPrato categoria) {
        List<Prato> pratos = Prato.list("categoria", categoria);
        return Response.ok(pratos).build();
    }

    @GET
    @Path("/buscar/por-faixa-de-preco")
    @Operation(summary = "Buscar pratos por faixa de preço")
    public Response findByFaixaDePreco(
            @Parameter(description = "Preço mínimo") @QueryParam("min") BigDecimal min,
            @Parameter(description = "Preço máximo") @QueryParam("max") BigDecimal max) {
        List<Prato> pratos = Prato.list("preco between ?1 and ?2", min, max);
        return Response.ok(pratos).build();
    }
}