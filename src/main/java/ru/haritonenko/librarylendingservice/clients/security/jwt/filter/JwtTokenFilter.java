package ru.haritonenko.librarylendingservice.clients.security.jwt.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.haritonenko.librarylendingservice.clients.security.custom.authentification.AuthClient;
import ru.haritonenko.librarylendingservice.clients.security.jwt.manager.JwtTokenManager;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenManager jwtTokenManager;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            AuthClient client = jwtTokenManager.getAuthClientFromToken(authorizationHeader.substring(7));
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    client,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority(client.getRole()))
            );
            SecurityContextHolder.getContext().setAuthentication(token);
        } catch (Exception ex) {
            log.warn("Error while reading jwt", ex);
        }

        filterChain.doFilter(request, response);
    }
}
