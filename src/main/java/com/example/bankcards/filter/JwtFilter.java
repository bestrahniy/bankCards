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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("Inside Jwt filter");
        try {
            final String authorizationHeader = request.getHeader(AUTHORIZATION);
            String jwt = null;
            String login = null;

            if (Objects.nonNull(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                log.info("JWT Tokwn ONLY: " + jwt);
                login = jwtHelper.extractLogin(jwt);
            }

            log.info("Security Context: " + SecurityContextHolder.getContext().getAuthentication());

            if (Objects.nonNull(login) && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.info("Context username:" + login);
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(login);
                log.info("Context user details: " + userDetails);
                boolean isTokenValidated = jwtHelper.validateToken(jwt, userDetails);
                log.info("Is token validated: " + isTokenValidated);

                if (isTokenValidated) {
                    log.info("UerDetails authorities: " + userDetails.getAuthorities());
    
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                    );

                    usernamePasswordAuthenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    
                    log.debug("User {} authenticated successfully", login);
                } else {
                    log.warn("Invalid JWT token for user: {}", login);
                }
            }
        } catch (ExpiredJwtException jwtException) {
            log.error("JWT token expired: {}", jwtException.getMessage());
            request.setAttribute("exception", jwtException);
        } catch (BadCredentialsException | UnsupportedJwtException | MalformedJwtException e) {
            log.error("Filter exception: {}", e.getMessage());
            request.setAttribute("exception", e);
        }
        filterChain.doFilter(request, response);
    }

}
