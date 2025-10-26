package br.com.financas.leitor_transacoes_ia.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro personalizado para processar autenticação JWT.
 * Quando security.enabled=false, cria um usuário mock para desenvolvimento local.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${security.enabled:false}")
    private boolean securityEnabled;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String LOCAL_USER = "local-user";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            if (!securityEnabled) {
                // Modo desenvolvimento: criar usuário mock
                setMockAuthentication();
            } else {
                // Modo produção: processar JWT real
                processJwtAuthentication(request);
            }
        } catch (Exception e) {
            log.error("Erro ao processar autenticação: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Configura autenticação mock para desenvolvimento local.
     */
    private void setMockAuthentication() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                LOCAL_USER,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("Usuário mock configurado: {}", LOCAL_USER);
        }
    }

    /**
     * Processa autenticação JWT real (será implementado pelo Spring Security OAuth2).
     * Este método é chamado quando security.enabled=true, mas o processamento real
     * é feito pelo OAuth2ResourceServerConfigurer.
     */
    private void processJwtAuthentication(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            log.debug("Token JWT recebido, comprimento: {}", token.length());
            // O processamento real do JWT é feito pelo Spring Security OAuth2
        } else {
            log.debug("Header Authorization não encontrado ou inválido");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Não aplicar filtro em endpoints públicos
        return path.startsWith("/actuator") || 
               path.startsWith("/swagger") || 
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/favicon.ico");
    }
}
