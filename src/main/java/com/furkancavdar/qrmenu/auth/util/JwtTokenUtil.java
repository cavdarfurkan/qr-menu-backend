package com.furkancavdar.qrmenu.auth.util;

import com.furkancavdar.qrmenu.auth.domain.Role;
import com.furkancavdar.qrmenu.auth.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JwtTokenUtil {

    private final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${spring.security.secret.key}")
    private String SECRET_KEY;

    @Value("${spring.security.token.access.expiration}")
    private long ACCESS_EXPIRATION;

    @Value("${spring.security.token.refresh.expiration}")
    private long REFRESH_EXPIRATION;

    private final String ISSUER = "qr-menu auth";

    private String generateToken(Map<String, Object> claims, User user, long expiration) {
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .issuer(ISSUER)
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .claims(claims)
                .claim("jti", jti)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateAccessToken(User user, String sessionId) {
        String roles = user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.joining(","));

        Map<String, Object> claims = new HashMap<>();
        claims.put("session_id", sessionId);
        claims.put("roles", roles);

        return generateToken(claims, user, ACCESS_EXPIRATION);
    }

    public String generateRefreshToken(User user, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("session_id", sessionId);

        return generateToken(claims, user, REFRESH_EXPIRATION);
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String extractSessionId(String token) {
        return getClaims(token).get("session_id", String.class);
    }

    public String extractJti(String token) {
        return getClaims(token).get("jti", String.class);
    }

    public long getExpirationTime(String token) {
        return getClaims(token).getExpiration().getTime();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username != null
                && username.equals(userDetails.getUsername())
                && getClaims(token).getIssuer().equals(ISSUER)
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }
}
