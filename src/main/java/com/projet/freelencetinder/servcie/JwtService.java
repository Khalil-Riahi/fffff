package com.projet.freelencetinder.servcie;

import java.util.*;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.projet.freelencetinder.dto.TokenPair;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Service @Slf4j
public class JwtService {

    @Value("${app.jwt.secret}")             private String jwtSecret;
    @Value("${app.jwt.expiration}")         private long jwtExpirationMs;
    @Value("${app.jwt.refresh-expiration}") private long refreshExpirationMs;

    /* ---------- Génération ---------- */
    public TokenPair generateTokenPair(Authentication auth) {
        return new TokenPair(
            generateToken(auth, jwtExpirationMs, new HashMap<>()),
            generateToken(auth, refreshExpirationMs, Map.of("tokenType", "refresh"))
        );
    }

    public String generateAccessToken(Authentication auth) {
        return generateToken(auth, jwtExpirationMs, new HashMap<>());
    }

    /* ---------- Validation ---------- */
    public boolean isRefreshToken(String token) {
        Claims c = parseClaims(token);
        return c != null && "refresh".equals(c.get("tokenType"));
    }

    public boolean validateForUser(String token, UserDetails user) {
        String sub = extractUsername(token);
        return sub != null && sub.equals(user.getUsername());
    }

    public String extractUsername(String token) {
        Claims c = parseClaims(token);
        return c == null ? null : c.getSubject();
    }

    /* ---------- Core ---------- */
    private String generateToken(Authentication auth, long exp, Map<String, Object> claims) {
        UserDetails p = (UserDetails) auth.getPrincipal();
        Date now = new Date();
        Date end = new Date(now.getTime() + exp);

        return Jwts.builder()
            .setHeaderParam("typ", "JWT")
            .setClaims(claims)
            .setSubject(p.getUsername())
            .setIssuedAt(now)
            .setExpiration(end)
            .signWith(signKey())
            .compact();
    }

    private Claims parseClaims(String t) {
        try {
            return Jwts.parserBuilder().setSigningKey(signKey()).build()
                       .parseClaimsJws(t).getBody();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private SecretKey signKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
