package com.ssafy.vibe.auth.jwt;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey secretKey;
    private final Long expiredMs;


    public JwtUtil(@Value("${APPLICATION_SECURITY_JWT_SECRET_KEY}") String secretKey,
                   @Value("${APPLICATION_SECURITY_JWT_ACCESS_TOKEN_EXPIRATION}") Long expiredMs) {
        this.secretKey=new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
        this.expiredMs=expiredMs;
    }

    public String createJwt(Long userId, String email, String username, String provider, String providerUid) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("email", email)
                .claim("username", username)
                .claim("provider", provider)
                .claim("providerUid", providerUid)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs * 1000))
                .signWith(secretKey)
                .compact();
    }

    public Long getUserId(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId", Long.class);
    }

    public String getEmail(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("email", String.class);
    }

    public String getNickname(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("nickname", String.class);
    }

    public String getProvider(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("provider", String.class);
    }

    public String getProviderUid(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("providerUid", String.class);
    }

    public boolean isValidAuthorization(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.startsWith("Bearer ");
    }

    public boolean isExpired(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }
}
