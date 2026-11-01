package br.edu.ifsp.kaluana.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisRateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final long maxAttempts;
    private final Duration window;

    public RedisRateLimiter(
            StringRedisTemplate redisTemplate,
            @Value("${app.rate-limit.max-attempts}") long maxAttempts,
            @Value("${app.rate-limit.window-seconds}") long windowSeconds) {
        this.redisTemplate = redisTemplate;
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    /**
     * Incrementa o contador da chave e devolve true se ainda está dentro do
     * limite. INCR + EXPIRE do Redis é o padrão usual para esse tipo de
     * janela fixa; não é perfeito sob concorrência extrema no exato instante
     * da criação da chave, mas é suficiente para limitar força bruta, que é
     * o objetivo aqui, não uma contagem financeira exata.
     */
    public boolean tryConsume(String key) {
        return tryConsume(key, maxAttempts, window);
    }

    /** Mesma janela fixa, com limite próprio (ex.: chamadas ao assistente de IA por usuário). */
    public boolean tryConsume(String key, long limit, Duration limitWindow) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, limitWindow);
        }
        return count != null && count <= limit;
    }
}