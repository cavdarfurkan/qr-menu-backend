package com.furkancavdar.qrmenu.auth.application.service;

import com.furkancavdar.qrmenu.auth.application.port.in.JwtTokenUseCase;
import com.furkancavdar.qrmenu.auth.application.port.in.dto.JwtTokenPair;
import com.furkancavdar.qrmenu.auth.domain.User;
import com.furkancavdar.qrmenu.auth.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JwtTokenService implements JwtTokenUseCase {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenService.class);
    private final JwtTokenUtil jwtTokenUtil;

    @Value("${spring.security.token.access.expiration}")
    private long ACCESS_EXPIRATION;

    @Value("${spring.security.token.refresh.expiration}")
    private long REFRESH_EXPIRATION;

    /**
     * <ul>
     * <li>Key Type: {@code String}</li>
     * <li>Key Name: {@code {jti}}</li>
     * <li>Value: {@code reason} (optional)</li>
     * <li>TTL: {@code expirationTime}</li>
     * </ul>
     *
     * @since 1.0.0
     */
    String BLACKLIST = "blacklist";

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void blacklistJti(String jti, String reason, long expirationTime) {
        String key = String.join(":", BLACKLIST, jti);
        stringRedisTemplate.opsForValue().set(key, reason, expirationTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isJtiBlacklisted(String jti) {
        String key = String.format(BLACKLIST, jti);
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    @Override
    public JwtTokenPair generateTokenPair(User user, String sessionId) {
        String accessToken = jwtTokenUtil.generateAccessToken(user, sessionId);
        String refreshToken = jwtTokenUtil.generateRefreshToken(user, sessionId);
        return new JwtTokenPair(accessToken, refreshToken);
    }
}
