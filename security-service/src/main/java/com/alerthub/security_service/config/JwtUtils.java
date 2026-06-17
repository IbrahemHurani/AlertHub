package com.alerthub.security_service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {

    @Value("${alerthub.jwt.secret}")
    private  String jwtSecret ;

    @Value("${alerthub.jwt.expiration-ms}")
    private int jwtExpirationMs ; 

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
//this method generates a JWT token for a given user ID and list of roles. It sets the subject, claims, issued date, expiration date, and signs the token using the HS256 algorithm with the secret key.
    public String generateJwtToken(Long userId, List<String> roles) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
//this method parses a JWT token and retrieves the claims contained within it. It uses the secret key to validate the token's signature and returns the claims if the token is valid.
    public Claims getClaimsFromJwtToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}