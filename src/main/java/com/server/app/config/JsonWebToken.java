package com.server.app.config;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.server.app.entities.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JsonWebToken {

    @Value("${security.jwt.expiration-time}")
    private long tokenTime;

    @Value("${security.jwt.secret-key}")
    private String tokenSecret;

    private SecretKey getTokenKey() {
        byte[] keyBytes = Decoders.BASE64.decode(tokenSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(User user) {
        Map<String, Object> claims = new LinkedHashMap<>();

        claims.put("id", user.getId());

        long currentTime = System.currentTimeMillis();

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(currentTime))
                .expiration(new Date(currentTime + tokenTime))
                .signWith(getTokenKey())
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getTokenKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Integer extractUserId(String token) {
        Claims claims = extractClaims(token);

        return claims.get("id", Integer.class);
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractClaims(token);

            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException exception) {
            return true;
        }
    }
}