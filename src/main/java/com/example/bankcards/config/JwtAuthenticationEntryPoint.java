package com.example.bankcards.config;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Authentication entry point for JWT-based security
 * Handles unauthorized requests by sending a 401 JSON response
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Commences the authentication process for unauthorized requests.
     * Sends a JSON error response and logs the attempt.
     *
     * @param request The incoming request.
     * @param response The response to send.
     * @param authException The authentication exception.
     * @throws IOException If response writing fails.
     * @throws ServletException If servlet error occurs.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        
        log.warn("Unauthorized request: {} {} | reason: {}", request.getMethod(),
                request.getRequestURI(), authException.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> data = new HashMap<>();
        OutputStream outputStream = response.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();

        data.put("message", authException.getMessage());

        try (outputStream) {
            mapper.writeValue(outputStream, data);
            outputStream.flush();
            log.debug("Sent unauthorized response for request: {}", request.getRequestURI());
        } catch (IOException exception) {
            log.error("Error writing unauthorized response", exception);
            throw exception;
        }
    }

}
