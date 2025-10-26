package br.com.financas.leitor_transacoes_ia.config;

import br.com.financas.leitor_transacoes_ia.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração de segurança Spring Security.
 * Suporta modo desenvolvimento (security.enabled=false) e produção (security.enabled=true).
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${security.enabled:false}")
    private boolean securityEnabled;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configurando segurança - enabled: {}", securityEnabled);

        if (!securityEnabled) {
            // Modo desenvolvimento: desabilitar autenticação
            return configureDevelopmentMode(http);
        } else {
            // Modo produção: habilitar OAuth2 Resource Server
            return configureProductionMode(http);
        }
    }

    /**
     * Configuração para modo desenvolvimento (sem autenticação).
     */
    private SecurityFilterChain configureDevelopmentMode(HttpSecurity http) throws Exception {
        log.info("Configurando modo desenvolvimento - autenticação desabilitada");
        
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**", "/favicon.ico")
                .permitAll()
                .anyRequest().permitAll() // Permitir todas as requisições em desenvolvimento
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    /**
     * Configuração para modo produção (com autenticação OAuth2).
     */
    private SecurityFilterChain configureProductionMode(HttpSecurity http) throws Exception {
        log.info("Configurando modo produção - OAuth2 Resource Server habilitado");
        
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**", "/favicon.ico")
                .permitAll()
                .requestMatchers("/api/v1/leitor/**").authenticated() // Proteger endpoints da API
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    /**
     * Configuração CORS para trabalhar com autenticação.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir origens específicas (ajustar conforme necessário)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        // Headers necessários para autenticação
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "x-api-gateway-request-id",
            "x-forwarded-for",
            "x-amzn-trace-id"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
