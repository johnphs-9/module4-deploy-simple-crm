package sg.edu.ntu.simple_crm_api.security;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT authentication filter (Assignment PT2 §2, L34-38).
 * <ul>
 * <li>Reads {@code Authorization: Bearer <token>}.</li>
 * <li>If the token is valid, sets the authentication in the SecurityContext
 * (authorities mapped from the existing UserDetailsService, fallback
 * ROLE_USER).</li>
 * <li>invalid or unhandled token: no authentication set; context cleared only
 * if validation threw — always continues the chain</li>
 * </ul>
 * Instantiated inline in {@code SecurityConfig} (not a bean) to avoid double
 * registration.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length());
            try {
                if (jwtService.isTokenValid(token)) {
                    String username = jwtService.extractUsername(token);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username, null, authoritiesFor(username));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (RuntimeException ex) {
                SecurityContextHolder.clearContext(); // treat as unauthenticated, never 500
            }
        }
        // Missing, invalid, or valid token: always continue the chain (L37-38)
        chain.doFilter(request, response);
    }

    /**
     * Maps the token subject to the pt1 RBAC roles; unknown users default to
     * ROLE_USER (D2).
     */
    private Collection<? extends GrantedAuthority> authoritiesFor(String username) {
        try {
            UserDetails user = userDetailsService.loadUserByUsername(username);
            return user.getAuthorities();
        } catch (UsernameNotFoundException ex) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
    }
}