package br.edu.ifsp.kaluana.config;
import br.edu.ifsp.kaluana.security.JwtAuthenticationFilter;
import br.edu.ifsp.kaluana.security.RateLimitFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwetAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final String allowedOrigin;

    public SecurityConfig(
            JwtAuthenticationFilter jwetAuthenticationFilter,
            RateLimitFilter rateLimitFilter,
            @Value("${app.cors.allowed-origin}") String allowedOrigin){
        this.jwetAuthenticationFilter = jwetAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.allowedOrigin = allowedOrigin;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/pairing/validate-code").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedEntryPoint()))
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwetAuthenticationFilter, RateLimitFilter.class);

        return http.build();
    }

    /**
     * Sem token (ou com token expirado) a resposta padrão do Spring é 403.
     * Devolvemos 401 no mesmo formato do ApiError para o app saber que
     * precisa entrar de novo, em vez de achar que faltou permissão.
     */
    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"status\":401,\"message\":\"Sua sessão expirou. Entre novamente.\"}");
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // Só as origens configuradas (separadas por vírgula). O app nativo não
        // envia Origin; o CORS vale para o build web e ferramentas no navegador.
        corsConfiguration.setAllowedOrigins(Arrays.stream(allowedOrigin.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
