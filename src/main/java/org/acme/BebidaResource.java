package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.headers.Header;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/bebidas")
public class BebidaResource {

    private BebidaRepresentation rep(Bebida b) {
        return BebidaRepresentation.from(b);
    }

    private List<BebidaRepresentation> repList(List<Bebida> bebidas) {
        return bebidas.stream().map(this::rep).collect(Collectors.toList());
    }

    @GET
    @Operation(
            summary = "Retorna todas as bebidas",
            description = "Lista todas as bebidas cadastradas. Este endpoint está sujeito a rate limiting (10 requisições por hora)."
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de bebidas retornada com sucesso",
            headers = {
                    @Header(
                            name = "X-RateLimit-Limit",
                            description = "Número máximo de requisições permitidas por hora",
                            schema = @Schema(implementation = Integer.class, example = "10")
                    ),
                    @Header(
                            name = "X-RateLimit-Remaining",
                            description = "Número de requisições restantes na janela atual",
                            schema = @Schema(implementation = Integer.class, example = "9")
                    ),
                    @Header(
                            name = "X-RateLimit-Reset",
                            description = "Timestamp Unix (em segundos) quando o limite será resetado",
                            schema = @Schema(implementation = Long.class, example = "1732305600")
                    )
            },
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BebidaRepresentation.class, type = SchemaType.ARRAY)
            )
    )
    @APIResponse(
            responseCode = "429",
            description = "Rate limit excedido. Você fez muitas requisições em um curto período.",
            headers = {
                    @Header(
                            name = "X-RateLimit-Limit",
                            description = "Número máximo de requisições permitidas por hora",
                            schema = @Schema(implementation = Integer.class, example = "10")
                    ),
                    @Header(
                            name = "X-RateLimit-Remaining",
                            description = "Número de requisições restantes (0 quando bloqueado)",
                            schema = @Schema(implementation = Integer.class, example = "0")
                    ),
                    @Header(
                            name = "X-RateLimit-Reset",
                            description = "Timestamp Unix quando o limite será resetado",
                            schema = @Schema(implementation = Long.class, example = "1732305600")
                    ),
                    @Header(
                            name = "Retry-After",
                            description = "Número de segundos até que você possa fazer uma nova requisição",
                            schema = @Schema(implementation = Long.class, example = "3540")
                    )
            },
            content = @Content(
                    mediaType = "application/json",
                    example = "{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded. Try again later.\"}"
            )
    )
    public Response getAll() {
        return Response.ok(repList(Bebida.listAll())).build();
    }

    @GET
    @Path("{id}")
    @Operation(
            summary = "Retorna uma bebida específica pelo ID",
            description = "Busca uma bebida por ID. Sujeito a rate limiting (10 requisições/hora)."
    )
    @APIResponse(
            responseCode = "200",
            description = "Bebida encontrada com sucesso",
            headers = {
                    @Header(
                            name = "X-RateLimit-Limit",
                            description = "Limite máximo de requisições",
                            schema = @Schema(implementation = Integer.class, example = "10")
                    ),
                    @Header(
                            name = "X-RateLimit-Remaining",
                            description = "Requisições restantes",
                            schema = @Schema(implementation = Integer.class, example = "8")
                    ),
                    @Header(
                            name = "X-RateLimit-Reset",
                            description = "Timestamp de reset",
                            schema = @Schema(implementation = Long.class, example = "1732305600")
                    )
            },
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BebidaRepresentation.class)
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Bebida não encontrada"
    )
    @APIResponse(
            responseCode = "429",
            description = "Rate limit excedido",
            headers = {
                    @Header(name = "Retry-After", schema = @Schema(implementation = Long.class, example = "3540"))
            }
    )
    public Response getById(@PathParam("id") long id) {
        Bebida entity = Bebida.findById(id);
        if (entity == null) {
            return Response.status(404).build();
        }
        return Response.ok(rep(entity)).build();
    }

    @GET
    @Path("/search")
    @Operation(
            summary = "Busca bebidas por nome com paginação",
            description = "Pesquisa bebidas pelo nome com suporte a ordenação e paginação. Sujeito a rate limiting."
    )
    @APIResponse(
            responseCode = "200",
            description = "Resultados da busca retornados com sucesso",
            headers = {
                    @Header(name = "X-RateLimit-Limit", schema = @Schema(implementation = Integer.class)),
                    @Header(name = "X-RateLimit-Remaining", schema = @Schema(implementation = Integer.class)),
                    @Header(name = "X-RateLimit-Reset", schema = @Schema(implementation = Long.class))
            },
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SearchBebidaResponse.class)
            )
    )
    @APIResponse(
            responseCode = "429",
            description = "Rate limit excedido",
            headers = {
                    @Header(name = "Retry-After", schema = @Schema(implementation = Long.class))
            }
    )
    public Response search(
            @QueryParam("q") @Parameter(description = "Termo de busca (nome da bebida)", example = "coca") String q,
            @QueryParam("sort") @DefaultValue("id") @Parameter(description = "Campo para ordenação", example = "nome") String sort,
            @QueryParam("direction") @DefaultValue("asc") @Parameter(description = "Direção da ordenação", example = "asc") String direction,
            @QueryParam("page") @DefaultValue("1") @Parameter(description = "Número da página", example = "1") int page,
            @QueryParam("size") @DefaultValue("10") @Parameter(description = "Tamanho da página", example = "10") int size
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

        SearchBebidaResponse response = SearchBebidaResponse.from(bebidas, q, sort, direction, page, size, totalElements, totalPages);
        return Response.ok(response).build();
    }

    @POST
    @Transactional
    @Operation(
            summary = "Cria uma nova bebida",
            description = "Cria uma nova bebida no sistema com suporte a idempotência."
    )
    @APIResponse(
            responseCode = "201",
            description = "Bebida criada com sucesso (primeira execução)",
            headers = {
                    @Header(
                            name = "Idempotency-Key",
                            description = "A chave de idempotência usada nesta requisição",
                            schema = @Schema(implementation = String.class, example = "550e8400-e29b-41d4-a716-446655440000")
                    ),
                    @Header(
                            name = "X-Idempotency-Message",
                            description = "Mensagem informativa sobre o processamento",
                            schema = @Schema(implementation = String.class, example = "Requisição processada com sucesso. Esta resposta será armazenada por 24 horas.")
                    )
            },
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BebidaRepresentation.class)
            )
    )
    @APIResponse(
            responseCode = "201",
            description = "Requisição duplicada detectada - idempotência aplicada",
            headers = {
                    @Header(
                            name = "X-Idempotent-Replayed",
                            description = "Indica que esta é uma requisição duplicada",
                            schema = @Schema(implementation = String.class, example = "true")
                    ),
                    @Header(
                            name = "X-Idempotency-Message",
                            description = "Mensagem explicativa sobre a duplicação",
                            schema = @Schema(implementation = String.class, example = "Esta requisição foi processada anteriormente. Retornando resposta armazenada.")
                    ),
                    @Header(
                            name = "Idempotency-Key",
                            description = "A chave duplicada que foi detectada",
                            schema = @Schema(implementation = String.class, example = "550e8400-e29b-41d4-a716-446655440000")
                    )
            },
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BebidaRepresentation.class)
            )
    )
    @Parameter(
            name = "Idempotency-Key",
            description = "Chave única para garantir idempotência. Use UUID para garantir unicidade.",
            required = false,
            in = ParameterIn.HEADER,
            schema = @Schema(implementation = String.class, example = "550e8400-e29b-41d4-a716-446655440000")
    )
    public Response insert(@Valid Bebida bebida) {
        Bebida.persist(bebida);
        return Response.status(201).entity(rep(bebida)).build();
    }


    @DELETE
    @Path("{id}")
    @Transactional
    @Operation(
            summary = "Exclui uma bebida",
            description = "Remove uma bebida do sistema pelo ID. Sujeito a rate limiting."
    )
    @APIResponse(
            responseCode = "204",
            description = "Bebida excluída com sucesso",
            headers = {
                    @Header(name = "X-RateLimit-Limit", schema = @Schema(implementation = Integer.class)),
                    @Header(name = "X-RateLimit-Remaining", schema = @Schema(implementation = Integer.class)),
                    @Header(name = "X-RateLimit-Reset", schema = @Schema(implementation = Long.class))
            }
    )
    @APIResponse(
            responseCode = "404",
            description = "Bebida não encontrada"
    )
    @APIResponse(
            responseCode = "429",
            description = "Rate limit excedido",
            headers = {
                    @Header(name = "Retry-After", schema = @Schema(implementation = Long.class))
            }
    )
    public Response delete(@PathParam("id") long id) {
        if (Bebida.deleteById(id)) {
            return Response.noContent().build();
        }
        return Response.status(404).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    @Operation(
            summary = "Atualiza uma bebida",
            description = "Atualiza os dados de uma bebida existente. Sujeito a rate limiting."
    )
    @APIResponse(
            responseCode = "200",
            description = "Bebida atualizada com sucesso",
            headers = {
                    @Header(name = "X-RateLimit-Limit", schema = @Schema(implementation = Integer.class)),
                    @Header(name = "X-RateLimit-Remaining", schema = @Schema(implementation = Integer.class)),
                    @Header(name = "X-RateLimit-Reset", schema = @Schema(implementation = Long.class))
            },
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BebidaRepresentation.class)
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Bebida não encontrada"
    )
    @APIResponse(
            responseCode = "400",
            description = "Dados inválidos"
    )
    @APIResponse(
            responseCode = "429",
            description = "Rate limit excedido",
            headers = {
                    @Header(name = "Retry-After", schema = @Schema(implementation = Long.class))
            }
    )
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
