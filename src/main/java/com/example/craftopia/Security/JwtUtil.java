package com.example.craftopia.Security;

import com.example.craftopia.Entity.Role;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Component
public class JwtUtil {
    @Value("${jwt.secret}") private String secret;
    @Value("${jwt.expiration}") private long expiration;

    public String generateToken(String email, Set<Role> roles) {
        Date now = new Date();

        System.out.println(roles);

        List<String> roleNames = roles.stream()
                .map(role -> role.getName().name()) // e.g., "ROLE_ADMIN"
                .toList();

        System.out.println(roleNames);

        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roleNames)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody());
    }
    public boolean validate(String token, String userEmail) {
        return extractEmail(token).equals(userEmail) && !extractClaim(token, Claims::getExpiration).before(new Date());
    }
    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }
}
