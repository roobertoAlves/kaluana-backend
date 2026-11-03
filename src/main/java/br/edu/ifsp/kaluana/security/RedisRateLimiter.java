package br.edu.ifsp.kaluana.security;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisRateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final long maxAttempts;
    private final Duration window;

    public RedisRateLimiter(
            StringRedisTemplate redisTemplate,
            RateLimitProperties properties) {
        this.redisTemplate = redisTemplate;
        this.maxAttempts = properties.maxAttempts();
        this.window = Duration.ofSeconds(properties.windowSeconds());
    }


    public boolean tryConsume(String key) {
        return tryConsume(key, maxAttempts, window);
    }

    public boolean tryConsume(String key, long limit, Duration limitWindow) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, limitWindow);
        }
        return count != null && count <= limit;
    }
}
