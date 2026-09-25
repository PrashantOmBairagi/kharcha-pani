package com.prashant.kharchapaniapplication.security;

import com.prashant.kharchapaniapplication.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${jwt.accessSecret}")
    private String jwtAccessSecret;

    @Value("${jwt.accessExpiration}")
    private long jwtAccessExpiration;

    @Value("${jwt.refreshSecret}")
    private String jwtRefreshSecret;

    @Value("${jwt.refreshExpiration}")
    private long jwtRefreshExpiration;

    private SecretKey getSecretKey(String jwtSecret) {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {

        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtAccessExpiration))
                .signWith(getSecretKey(jwtAccessSecret))
                .compact();
    }
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtRefreshExpiration))
                .signWith(getSecretKey(jwtRefreshSecret))
                .compact();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaim(token, Claims::getSubject,jwtRefreshSecret)) ;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver , String jwtSecret) {
        final Claims claims = extractAllClaims(token,jwtSecret);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token,String jwtSecret) {
        return Jwts.parser()
                .verifyWith(getSecretKey(jwtSecret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateRefreshToken(String refreshToken) {
        return extractClaim(refreshToken, Claims::getExpiration,jwtRefreshSecret).after(new Date());
    }

    public Date extractExpiration(String token) {return extractClaim(token, Claims::getExpiration,jwtAccessSecret);}

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token) {
        return (!isTokenExpired(token));
    }
}
