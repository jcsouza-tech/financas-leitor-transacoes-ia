package br.com.financas.leitor_transacoes_ia.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Classe utilitária para obter informações do usuário autenticado do SecurityContext.
 * Extrai o userId (sub claim) do JWT token do Cognito.
 */
@Component
public class UserContext {

    private static final String DEFAULT_USER_ID = "local-user";
    private static final String SUB_CLAIM = "sub";

    /**
     * Obtém o ID do usuário atual do SecurityContext.
     * 
     * @return userId extraído do JWT token ou "local-user" para desenvolvimento local
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return DEFAULT_USER_ID;
        }

        // Para desenvolvimento local (quando security.enabled=false)
        if (authentication instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken) {
            return authentication.getName();
        }

        // Para JWT tokens do Cognito
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
            Jwt jwt = jwtToken.getToken();
            return jwt.getClaimAsString(SUB_CLAIM);
        }

        // Fallback para outros tipos de autenticação
        return authentication.getName();
    }

    /**
     * Verifica se o usuário está autenticado.
     * 
     * @return true se autenticado, false caso contrário
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
            && !"anonymousUser".equals(authentication.getName());
    }

    /**
     * Obtém o nome do usuário (email) do token JWT.
     * 
     * @return email do usuário ou null se não disponível
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
            Jwt jwt = jwtToken.getToken();
            return jwt.getClaimAsString("email");
        }
        
        return null;
    }
}
