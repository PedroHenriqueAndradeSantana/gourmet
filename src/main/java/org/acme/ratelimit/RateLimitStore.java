package org.acme.ratelimit;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class RateLimitStore {

    private final Map<String, ClientRateLimit> store = new ConcurrentHashMap<>();

    public static class ClientRateLimit {
        public int requests;
        public Instant windowStart;

        public ClientRateLimit(int requests, Instant windowStart) {
            this.requests = requests;
            this.windowStart = windowStart;
        }
    }

    public ClientRateLimit getOrCreate(String clientId) {
        return store.computeIfAbsent(clientId, k -> new ClientRateLimit(0, Instant.now()));
    }

    public void increment(String clientId) {
        ClientRateLimit limit = getOrCreate(clientId);
        limit.requests++;
    }

    public void reset(String clientId) {
        store.put(clientId, new ClientRateLimit(0, Instant.now()));
    }

    public void cleanup() {
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        store.entrySet().removeIf(entry -> entry.getValue().windowStart.isBefore(oneHourAgo));
    }
}
