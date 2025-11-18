package org.acme.ratelimit;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Provider
public class RateLimitFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    RateLimitStore rateLimitStore;

    // Configurações: 10 requisições por hora por IP (mudado de 100 para testar mais rápido)
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_SECONDS = 3600; // 1 hora

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        System.out.println("=== RateLimitFilter: Método = " + requestContext.getMethod());

        // Aplica rate limit apenas no endpoint /bebidas (mudado de /pedidos)
        String path = requestContext.getUriInfo().getPath();
        System.out.println("=== Path da requisição: " + path);

        if (!path.startsWith("/bebidas")) {
            System.out.println("=== Path não começa com 'bebidas', pulando rate limit...");
            return;
        }

        System.out.println("=== Aplicando rate limit em bebidas...");

        String clientId = getClientIdentifier(requestContext);
        System.out.println("=== Cliente ID: " + clientId);

        RateLimitStore.ClientRateLimit limit = rateLimitStore.getOrCreate(clientId);
        Instant now = Instant.now();
        long secondsSinceWindowStart = Duration.between(limit.windowStart, now).getSeconds();

        // Se passou o período da janela, reseta o contador
        if (secondsSinceWindowStart >= WINDOW_SECONDS) {
            rateLimitStore.reset(clientId);
            limit = rateLimitStore.getOrCreate(clientId);
            System.out.println("=== Janela resetada (1 hora passou)");
        }

        int remaining = Math.max(0, MAX_REQUESTS - limit.requests);

        System.out.println("=== Requisições feitas: " + limit.requests + "/" + MAX_REQUESTS);
        System.out.println("=== Requisições restantes: " + remaining);

        // Adiciona headers informativos
        requestContext.setProperty("X-RateLimit-Limit", MAX_REQUESTS);
        requestContext.setProperty("X-RateLimit-Remaining", remaining);
        requestContext.setProperty("X-RateLimit-Reset", limit.windowStart.plusSeconds(WINDOW_SECONDS).getEpochSecond());

        // Se excedeu o limite, retorna 429
        if (limit.requests >= MAX_REQUESTS) {
            System.out.println("=== ❌ RATE LIMIT ATINGIDO! Bloqueando requisição...");
            Response response = Response
                    .status(429)
                    .entity("{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded. Try again later.\"}")
                    .header("X-RateLimit-Limit", MAX_REQUESTS)
                    .header("X-RateLimit-Remaining", 0)
                    .header("X-RateLimit-Reset", limit.windowStart.plusSeconds(WINDOW_SECONDS).getEpochSecond())
                    .header("Retry-After", WINDOW_SECONDS - secondsSinceWindowStart)
                    .build();
            requestContext.abortWith(response);
            return;
        }

        // Incrementa o contador
        rateLimitStore.increment(clientId);
        System.out.println("=== ✅ Requisição permitida, incrementando contador");
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // Adiciona headers de rate limit na resposta
        Object limit = requestContext.getProperty("X-RateLimit-Limit");
        Object remaining = requestContext.getProperty("X-RateLimit-Remaining");
        Object reset = requestContext.getProperty("X-RateLimit-Reset");

        if (limit != null) {
            responseContext.getHeaders().add("X-RateLimit-Limit", limit);
            responseContext.getHeaders().add("X-RateLimit-Remaining", remaining);
            responseContext.getHeaders().add("X-RateLimit-Reset", reset);
        }
    }

    private String getClientIdentifier(ContainerRequestContext requestContext) {
        // Tenta pegar o IP real do cliente
        String forwarded = requestContext.getHeaderString("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }

        String realIp = requestContext.getHeaderString("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }

        // Fallback para identificador genérico (em produção deveria usar o IP real)
        return "default-client";
    }
}
