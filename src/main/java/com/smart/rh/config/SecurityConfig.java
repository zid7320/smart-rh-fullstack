package com.smart.rh.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.rh.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.Instant;
import java.util.Map;

/**
 * Spring Security 6 — stateless JWT + RBAC configuration.
 *
 * <ul>
 *   <li>No HTTP sessions — every request must carry a valid Bearer token.</li>
 *   <li>{@link JwtAuthenticationFilter} populates the SecurityContext before
 *       the standard username/password filter.</li>
 *   <li>Method-level RBAC via {@code @PreAuthorize} / {@code @PostAuthorize}.</li>
 *   <li>Custom JSON responses for 401 (missing/invalid token) and 403 (wrong role).</li>
 * </ul>
 *
 * Spring Boot auto-detects our {@link com.smart.rh.security.UserDetailsServiceImpl}
 * bean + the {@link PasswordEncoder} bean and builds a {@code DaoAuthenticationProvider}
 * automatically — no explicit wiring needed here.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** Paths that never require a JWT. */
    private static final String[] PUBLIC_PATHS = {
            "/api/auth/**",
            "/api/health",
            "/swagger-ui/**", "/swagger-ui.html",
            "/api-docs/**",   "/v3/api-docs/**",
            "/h2-console/**",
            "/actuator/health",
            "/ws/**"    // WebSocket handshake + SockJS HTTP polling fallback
    };

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ObjectMapper            objectMapper;

    // ── Filter chain ──────────────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_PATHS).permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((req, res, e) ->
                            writeErrorJson(res, HttpServletResponse.SC_UNAUTHORIZED,
                                    "Unauthorized", e.getMessage(), req.getRequestURI()))
                    .accessDeniedHandler((req, res, e) ->
                            writeErrorJson(res, HttpServletResponse.SC_FORBIDDEN,
                                    "Forbidden", e.getMessage(), req.getRequestURI()))
            );

        return http.build();
    }

    // ── Beans ─────────────────────────────────────────────────────────────────

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void writeErrorJson(HttpServletResponse res,
                                 int status, String error,
                                 String message, String path) {
        try {
            res.setStatus(status);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding("UTF-8");
            res.getWriter().write(objectMapper.writeValueAsString(Map.of(
                    "timestamp", Instant.now().toString(),
                    "status",    status,
                    "error",     error,
                    "message",   message != null ? message : "",
                    "path",      path
            )));
        } catch (Exception ignored) { /* best-effort JSON response */ }
    }
}
