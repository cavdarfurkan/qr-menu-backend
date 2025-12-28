package com.furkancavdar.qrmenu.auth.adapter.persistence.repository;

import com.furkancavdar.qrmenu.auth.application.port.out.SessionRepositoryPort;
import com.furkancavdar.qrmenu.auth.domain.SessionMetadata;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionAdapter implements SessionRepositoryPort {
  /**
   * Use this String only by formatting.
   *
   * <p>Format: {{@code username}}
   *
   * <p>
   *
   * <ul>
   *   <li>Key Type: {@code Set}
   *   <li>Member: {@code sessionId}
   * </ul>
   *
   * @since 1.0.0
   */
  private static final String USER_SESSIONS = "user:%s:sessions";

  /**
   *
   *
   * <ul>
   *   <li>Key Type: {@code Hash}
   *   <li>Field (Hash Key): {@code sessionId}
   *   <li>Value: {@link SessionMetadata} as JSON
   * </ul>
   *
   * @since 1.0.0
   */
  private static final String ALL_SESSIONS = "sessions";

  private final RedisTemplate<String, Object> redisTemplate;
  private final StringRedisTemplate stringRedisTemplate;

  /**
   * Save the {@link SessionMetadata} to {@value USER_SESSIONS} and {@value ALL_SESSIONS}
   *
   * @author Furkan Çavdar
   * @since 1.0.0
   */
  @Override
  public void saveSession(String username, SessionMetadata sessionMetadata) {
    String userSessionsKey = String.format(USER_SESSIONS, username);

    stringRedisTemplate.opsForSet().add(userSessionsKey, sessionMetadata.getSessionId());
    redisTemplate
        .<String, SessionMetadata>opsForHash()
        .put(ALL_SESSIONS, sessionMetadata.getSessionId(), sessionMetadata);
  }

  /**
   * Get and return {@link SessionMetadata} from {@value ALL_SESSIONS}
   *
   * @author Furkan Çavdar
   * @since 1.0.0
   */
  @Override
  public Optional<SessionMetadata> findBySessionId(String sessionId) {
    SessionMetadata sessionMetadata =
        redisTemplate.<String, SessionMetadata>opsForHash().get(ALL_SESSIONS, sessionId);
    return Optional.ofNullable(sessionMetadata);
  }

  /**
   * Get all the sessions user owns.
   *
   * @param username Username of the user
   * @return {@code List<SessionMetadata>}
   * @author Furkan Çavdar
   * @since 1.0.0
   */
  @Override
  public List<SessionMetadata> findAllSessionsByUsername(String username) {
    String userSessionsKey = String.format(USER_SESSIONS, username);
    Set<String> sessionIds = stringRedisTemplate.opsForSet().members(userSessionsKey);

    if (sessionIds == null || sessionIds.isEmpty()) {
      return Collections.emptyList();
    }

    return redisTemplate.<String, SessionMetadata>opsForHash().multiGet(ALL_SESSIONS, sessionIds);
  }

  /**
   * Delete session entries from both {@value USER_SESSIONS} and {@value ALL_SESSIONS}.
   *
   * @author Furkan Çavdar
   * @since 1.0.0
   */
  @Override
  public void deleteBySessionId(String sessionId) {
    SessionMetadata sessionMetadata =
        redisTemplate.<String, SessionMetadata>opsForHash().get(ALL_SESSIONS, sessionId);
    if (sessionMetadata == null) {
      throw new NullPointerException("sessionId " + sessionId + " not found");
    }

    String username = sessionMetadata.getUsername();
    String userSessionsKey = String.format(USER_SESSIONS, username);

    stringRedisTemplate.opsForSet().remove(userSessionsKey, sessionId);
    redisTemplate.opsForHash().delete(ALL_SESSIONS, sessionId);
  }

  /**
   * Get the {@link SessionMetadata} and compare the {@code expiresAt} field with now.
   *
   * @author Furkan Çavdar
   * @since 1.0.0
   */
  @Override
  public boolean isSessionExpired(String sessionId) {
    SessionMetadata session =
        redisTemplate.<String, SessionMetadata>opsForHash().get(ALL_SESSIONS, sessionId);
    if (session == null) {
      throw new NullPointerException("sessionId " + sessionId + " not found");
    }
    return session.getExpiresAt() < System.currentTimeMillis();
  }

  @Override
  public boolean isUserOwnsSession(String username, String sessionId) {
    String userSessionsKey = String.format(USER_SESSIONS, username);
    return Boolean.TRUE.equals(
        stringRedisTemplate.opsForSet().isMember(userSessionsKey, sessionId));
  }
}
