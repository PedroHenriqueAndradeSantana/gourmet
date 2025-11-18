package org.acme.idempotency;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import java.time.LocalDateTime;

@Entity
public class IdempotencyKey extends PanacheEntity {

    /**
     * A chave única de idempotência enviada pelo cliente
     * Exemplo: "550e8400-e29b-41d4-a716-446655440000"
     */
    @Column(unique = true, nullable = false)
    public String idempotencyKey;

    /**
     * Status HTTP da resposta original (ex: 201, 200)
     */
    @Column(nullable = false)
    public int statusCode;

    /**
     * Corpo da resposta JSON serializado
     * Armazenado como TEXT para suportar JSONs grandes
     */
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    public String responseBody;

    /**
     * Quando a chave foi criada (primeira requisição)
     */
    @Column(nullable = false)
    public LocalDateTime createdAt;

    /**
     * Quando a chave expira (24 horas após criação)
     */
    @Column(nullable = false)
    public LocalDateTime expiresAt;

    /**
     * Busca uma chave pelo valor
     * @param key - O valor da chave de idempotência
     * @return IdempotencyKey encontrado ou null
     */
    public static IdempotencyKey findByKey(String key) {
        return find("idempotencyKey", key).firstResult();
    }

    /**
     * Verifica se a chave já expirou (passou de 24h)
     * @return true se expirou, false caso contrário
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
