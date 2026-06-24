package ru.haritonenko.librarylendingservice.clients.security.jwt.manager;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.haritonenko.librarylendingservice.clients.security.custom.authentification.AuthClient;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenManager {

    @Value("${jwt.secret-key}")
    private String keyString;

    @Value("${jwt.lifetime}")
    private long expirationTime;

    private SecretKey key;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(keyString.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long clientId, String login, String role) {
        return Jwts.builder()
                .setSubject(login)
                .claim("clientId", clientId)
                .claim("role", role)
                .signWith(key, SignatureAlgorithm.HS256)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .compact();
    }

    public AuthClient getAuthClientFromToken(String jwt) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt)
                .getBody();

        Number clientId = claims.get("clientId", Number.class);

        return AuthClient.builder()
                .id(clientId.longValue())
                .login(claims.getSubject())
                .role(claims.get("role", String.class))
                .build();
    }
}
