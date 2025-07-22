package com.example.craftopia.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = req.getHeader("Authorization");
        String token = null;
        String email = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            email = jwtUtil.extractEmail(token);
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validate(token, email)) {
                System.out.println("JWT Claims: " + jwtUtil.extractAllClaims(token));

                List<String> roles = jwtUtil.extractClaim(token, claims -> {
                    Object raw = claims.get("roles");
                    if (raw instanceof List<?>) {
                        return ((List<?>) raw).stream()
                                .map(Object::toString)
                                .collect(Collectors.toList());
                    }
                    return null;
                });
                // Extract roles and handle null safely
//                List<String> roles = jwtUtil.extractClaim(token, claims -> claims.get("roles", List.class));
                System.out.println(roles);
                List<GrantedAuthority> authorities = (roles != null)
                        ? roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                        : List.of(); // default to empty list if roles are missing

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }


        chain.doFilter(req, res);
    }
}
