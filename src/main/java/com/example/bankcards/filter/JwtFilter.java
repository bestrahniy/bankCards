package com.example.bankcards.filter;

import java.io.IOException;
import java.util.Objects;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.bankcards.jwt.JwtHelper;
import com.example.bankcards.service.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT authentication filter for Spring Security.
 */
@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION = "Authorization";

    private final CustomUserDetailsService userDetailsService;
    private final JwtHelper jwtHelper;

    public JwtFilter(CustomUserDetailsService userDetailsService, JwtHelper jwtHelper) {
        this.userDetailsService = userDetailsService;
        this.jwtHelper = jwtHelper;
    }

    /**
     * Processes each HTTP request to validate JWT authentication.
     * 
     * Extracts JWT from Authorization header, validates it, loads user details,
     * and sets up Spring Security authentication context if validation succeeds.
     * 
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     * @param filterChain filter chain for continuing request processing
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader(AUTHORIZATION);
        log.trace("Authorization header present: {}", authorizationHeader != null);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found, continuing filter chain");
            filterChain.doFilter(request, response);
            return;
        }

        log.info("JWT authentication filter processing request to: {}", request.getRequestURI());

        try {
            String jwt = authorizationHeader.substring(7);
            log.debug("JWT token extracted (length: {} chars)", jwt.length());

            String login = jwtHelper.extractLogin(jwt);
            log.debug("Extracted login from JWT: {}", login);

            log.trace("Current Security Context authentication: {}", 
                SecurityContextHolder.getContext().getAuthentication());

            if (Objects.nonNull(login) && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.info("Authenticating user: {}", login);
                
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(login);
                log.debug("User details loaded for: {}", login);
                
                boolean isTokenValidated = jwtHelper.validateToken(jwt, userDetails);
                log.debug("Token validation result for {}: {}", login, isTokenValidated);

                if (isTokenValidated) {
                    log.info("User {} authenticated successfully via JWT", login);
                    log.trace("User authorities: {}", userDetails.getAuthorities());
    
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                    );

                    usernamePasswordAuthenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    log.info("Security context updated for user: {}", login);
                    
                } else {
                    log.warn("JWT token validation failed for user: {}", login);
                }
            } else {
                log.debug("User already authenticated or no login extracted");
            }
            
        } catch (ExpiredJwtException jwtException) {
            log.error("JWT token expired: {}", jwtException.getMessage());
            request.setAttribute("exception", jwtException);
            log.debug("Exception attribute set on request");
            
        } catch (BadCredentialsException | UnsupportedJwtException | MalformedJwtException e) {
            log.error("JWT filter exception: {}", e.getMessage());
            request.setAttribute("exception", e);
            log.debug("Exception attribute set on request");
            
        } catch (Exception e) {
            log.error("Unexpected error in JWT filter: {}", e.getMessage(), e);
            request.setAttribute("exception", e);
        }
        
        log.trace("Continuing filter chain after JWT processing");
        filterChain.doFilter(request, response);
    }

}