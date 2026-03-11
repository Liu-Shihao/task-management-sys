package com.finblock.tms.modules.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finblock.tms.common.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {

    private final ApiTokenService apiTokenService;
    private final String headerName;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private final AntPathRequestMatcher healthMatcher = new AntPathRequestMatcher("/api/health");
    private final AntPathRequestMatcher errorMatcher = new AntPathRequestMatcher("/error");

    public ApiTokenAuthenticationFilter(ApiTokenService apiTokenService, String headerName) {
        this.apiTokenService = apiTokenService;
        this.headerName = headerName;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return healthMatcher.matches(request) || errorMatcher.matches(request);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = request.getHeader(headerName);
        var maybeUser = apiTokenService.authenticate(token);
        if (maybeUser.isEmpty()) {
            unauthorized(response, "UNAUTHORIZED", "Missing or invalid API token");
            return;
        }

        AuthenticatedUser user = maybeUser.get();
        var auth = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(code, message, null));
    }
}
