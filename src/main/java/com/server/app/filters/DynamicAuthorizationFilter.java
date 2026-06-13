package com.server.app.filters;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.app.config.SecurityRules;
import com.server.app.dto.response.ExceptionResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class DynamicAuthorizationFilter extends OncePerRequestFilter {

    private final AntPathMatcher pathMatcher =
            new AntPathMatcher();

    private final ObjectMapper objectMapper;

    public DynamicAuthorizationFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String method = request.getMethod();
        String path = request.getRequestURI();

        if (SecurityRules.isIgnored(path)
                || SecurityRules.isPublic(method, path)
                || SecurityRules.isAuthOnly(method, path)) {

            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required"
            );
            return;
        }

        if (!isAuthorized(authentication, method, path)) {
            sendError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Acceso denegado: no tienes permisos para esta ruta: "
                            + path
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthorized(
            Authentication authentication,
            String method,
            String path
    ) {
        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority -> {

                    String value = authority.getAuthority();
                    String[] parts = value.split(":", 2);

                    if (parts.length != 2) {
                        return false;
                    }

                    String allowedMethod = parts[0];
                    String allowedPath = parts[1];

                    return method.equalsIgnoreCase(allowedMethod)
                            && pathMatcher.match(allowedPath, path);
                });
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        if (response.isCommitted()) {
            return;
        }

        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        ExceptionResponse error =
                new ExceptionResponse(status, message);

        response.getWriter().write(
                objectMapper.writeValueAsString(error)
        );
    }
}