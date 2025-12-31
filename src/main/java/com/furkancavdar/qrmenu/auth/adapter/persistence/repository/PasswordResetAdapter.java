package com.furkancavdar.qrmenu.auth.adapter.persistence.repository;

import com.furkancavdar.qrmenu.auth.application.port.out.PasswordResetRepositoryPort;
import com.furkancavdar.qrmenu.auth.domain.PasswordResetToken;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetAdapter implements PasswordResetRepositoryPort {

  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public void save(PasswordResetToken passwordResetToken) {
    log.info(
        "Saving password reset token for userId={}, expiresAt={}",
        passwordResetToken.userId(),
        passwordResetToken.expiresAt());

    Duration ttl = Duration.between(Instant.now(), passwordResetToken.expiresAt());
    if (ttl.isZero() || ttl.isNegative()) {
      log.warn(
          "Password reset token TTL is zero or negative (TTL={}, userId={}, expiresAt={}). Skipping Redis persistence.",
          ttl,
          passwordResetToken.userId(),
          passwordResetToken.expiresAt());
      return;
    }

    try {
      stringRedisTemplate
          .opsForValue()
          .set(passwordResetToken.token(), passwordResetToken.userId().toString(), ttl);
    } catch (Exception e) {
      log.error(
          "Failed to save password reset token to Redis (userId={}, expiresAt={}): {}",
          passwordResetToken.userId(),
          passwordResetToken.expiresAt(),
          e.getMessage(),
          e);
      throw new RuntimeException("Failed to persist password reset token", e);
    }
  }

  @Override
  public Optional<PasswordResetToken> findByToken(String token) {
    String userId = stringRedisTemplate.opsForValue().get(token);

    if (userId == null) {
      return Optional.empty();
    }

    long ttlSeconds = stringRedisTemplate.getExpire(token, TimeUnit.SECONDS);

    if (ttlSeconds <= 0) {
      return Optional.empty(); // expired or invalid
    }

    Instant expiresAt = Instant.now().plusSeconds(ttlSeconds);

    return Optional.of(new PasswordResetToken(token, Long.valueOf(userId), expiresAt));
  }

  @Override
  public void delete(String token) {
    log.info("Deleting password reset token");
    stringRedisTemplate.delete(token);
  }
}
