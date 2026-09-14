package br.edu.ifsp.kaluana.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.util.RateLimiter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of(
            "/api/auth/login",
            "api/pairing/validate-code",
            "api/auth/register/primary",
            "api/auth/register/responsible"
    );

    private static final int MAX_ATTEMPS = 10;
    private static final long WINDOW_MILLIS = 5* 60 * 1000L;

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Long>> attemptsByKey = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if(!LIMITED_PATHS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = clientIp(request) + ":" + path;
        long now = Instant.now().toEpochMilli();
        CopyOnWriteArrayList<Long> attempts = attemptsByKey.computeIfAbsent(key, ignored -> new CopyOnWriteArrayList<>());
        attempts.removeIf(timestamp -> now - timestamp > WINDOW_MILLIS);

        if(attempts.size() >= MAX_ATTEMPS) {

            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"message\":\"Muitas tentativas seguidas. Aguarde alguns minutos.\"}");
            return;
        }

        attempts.add(now);
        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded =  request.getHeader("X-Forwarded-For");
        if(forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
