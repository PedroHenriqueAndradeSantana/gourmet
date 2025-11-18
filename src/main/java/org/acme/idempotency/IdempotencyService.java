package org.acme.idempotency;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

/**
 * Serviço responsável por gerenciar chaves de idempotência
 */
@ApplicationScoped
public class IdempotencyService {

    /**
     * Busca uma chave existente ou retorna null
     * Se encontrar uma chave expirada, a deleta automaticamente
     *
     * @param key - Chave de idempotência
     * @return IdempotencyKey se existe e é válida, null caso contrário
     */
    @Transactional
    public IdempotencyKey findOrCreate(String key) {
        IdempotencyKey existing = IdempotencyKey.findByKey(key);

        if (existing != null) {
            // Verifica se expirou (passou de 24h)
            if (existing.isExpired()) {
                existing.delete();
                return null; // Chave expirada, pode processar novamente
            }
            return existing; // Chave válida, retorna para replay
        }

        return null; // Chave não existe, primeira vez
    }

    /**
     * Salva a resposta de uma requisição para futuras repetições
     *
     * @param key - Chave de idempotência
     * @param statusCode - Status HTTP da resposta (ex: 201)
     * @param responseBody - Corpo da resposta JSON
     */
    @Transactional
    public void saveResponse(String key, int statusCode, String responseBody) {
        IdempotencyKey idempotencyKey = new IdempotencyKey();
        idempotencyKey.idempotencyKey = key;
        idempotencyKey.statusCode = statusCode;
        idempotencyKey.responseBody = responseBody;
        idempotencyKey.createdAt = LocalDateTime.now();
        idempotencyKey.expiresAt = LocalDateTime.now().plusHours(24); // Expira em 24h
        idempotencyKey.persist();
    }

    /**
     * Remove chaves expiradas do banco (limpeza)
     * Pode ser chamado por um scheduler periódico
     */
    @Transactional
    public void cleanExpired() {
        IdempotencyKey.delete("expiresAt < ?1", LocalDateTime.now());
    }
}
