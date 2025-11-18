package org.acme.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class IdempotencyFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    IdempotencyService idempotencyService;

    @Inject
    ObjectMapper objectMapper; // Para serializar JSON corretamente

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final String IDEMPOTENCY_KEY_PROPERTY = "idempotency.key";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        System.out.println("=== IdempotencyFilter: Método = " + requestContext.getMethod());

        // Apenas aplica idempotência em requisições POST
        if (!"POST".equals(requestContext.getMethod())) {
            return;
        }

        String idempotencyKey = requestContext.getHeaderString(IDEMPOTENCY_KEY_HEADER);
        System.out.println("=== IdempotencyKey recebida: " + idempotencyKey);

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            System.out.println("=== Nenhuma chave de idempotência fornecida, pulando...");
            return;
        }

        IdempotencyKey stored = idempotencyService.findOrCreate(idempotencyKey);
        System.out.println("=== Chave já existe no banco? " + (stored != null));

        if (stored != null) {
            System.out.println("=== REPLAY! Retornando resposta armazenada");

            // Deserializa o JSON salvo de volta para objeto
            Object responseBody = objectMapper.readValue(stored.responseBody, Object.class);

            Response response = Response
                    .status(stored.statusCode)
                    .entity(responseBody)
                    .type(MediaType.APPLICATION_JSON)
                    .header("X-Idempotent-Replayed", "true")
                    .header("X-Idempotency-Message", "Esta requisição foi processada anteriormente. Retornando resposta armazenada.")
                    .header("Idempotency-Key", idempotencyKey)
                    .build();

            requestContext.abortWith(response);
            return;
        }

        System.out.println("=== Nova requisição, marcando para salvar");
        requestContext.setProperty(IDEMPOTENCY_KEY_PROPERTY, idempotencyKey);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String idempotencyKey = (String) requestContext.getProperty(IDEMPOTENCY_KEY_PROPERTY);

        if (idempotencyKey == null || !"POST".equals(requestContext.getMethod())) {
            return;
        }

        // Apenas salva se a resposta foi bem-sucedida (2xx)
        if (responseContext.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            System.out.println("=== Salvando resposta para chave: " + idempotencyKey);

            Object entity = responseContext.getEntity();

            // Serializa o objeto para JSON string
            String responseBodyJson;
            if (entity != null) {
                responseBodyJson = objectMapper.writeValueAsString(entity);
            } else {
                responseBodyJson = "{}";
            }

            System.out.println("=== JSON serializado: " + responseBodyJson);

            // Salva no banco
            idempotencyService.saveResponse(
                    idempotencyKey,
                    responseContext.getStatus(),
                    responseBodyJson
            );

            // Adiciona headers informativos na primeira execução
            responseContext.getHeaders().add("Idempotency-Key", idempotencyKey);
            responseContext.getHeaders().add("X-Idempotency-Message", "Requisição processada com sucesso. Esta resposta será armazenada por 24 horas.");
        }
    }
}
