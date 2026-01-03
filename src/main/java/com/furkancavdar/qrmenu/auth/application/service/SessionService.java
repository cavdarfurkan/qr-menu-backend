package com.furkancavdar.qrmenu.auth.application.service;

import com.furkancavdar.qrmenu.auth.application.exception.SessionOwnerException;
import com.furkancavdar.qrmenu.auth.application.exception.UnauthorizedException;
import com.furkancavdar.qrmenu.auth.application.port.in.JwtTokenUseCase;
import com.furkancavdar.qrmenu.auth.application.port.in.SessionUseCase;
import com.furkancavdar.qrmenu.auth.application.port.out.SessionRepositoryPort;
import com.furkancavdar.qrmenu.auth.domain.SessionMetadata;
import com.furkancavdar.qrmenu.auth.util.JwtTokenUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionService implements SessionUseCase {

  private static final Logger log = LoggerFactory.getLogger(SessionService.class);
  private final JwtTokenUseCase jwtTokenUseCase;
  private final JwtTokenUtil jwtTokenUtil;
  private final SessionRepositoryPort sessionRepository;

  @Override
  public List<SessionMetadata> getActiveSessions(String username) {
    return sessionRepository.findAllSessionsByUsername(username);
  }

  @Override
  public void terminateSession(String username, String sessionId) {
    if (username == null || sessionId == null) {
      throw new NullPointerException("Username and sessionId cannot be null");
    }

    if (!sessionRepository.isUserOwnsSession(username, sessionId)) {
      throw new SessionOwnerException("Session " + sessionId + " is not owned by user " + username);
    }

    SessionMetadata sessionMetadata =
        sessionRepository
            .findBySessionId(sessionId)
            .orElseThrow(() -> new NullPointerException("Session " + sessionId + " not found"));

    try {
      long expirationTime = (sessionMetadata.getExpiresAt() + 1000L) - System.currentTimeMillis();
      if (expirationTime > 0) {
        jwtTokenUseCase.blacklistJti(
            sessionMetadata.getRefreshJti(), "Session is terminated", expirationTime);
      }
    } catch (Exception e) {
      log.error("Session {} termination failed: {}", sessionId, e.getMessage());
      throw new RuntimeException("Session termination failed during JTI blacklisting", e);
    }
    sessionRepository.deleteBySessionId(sessionId);
  }

  @Override
  public void terminateAllOtherSessions(String username, String authHeader) {
    if (authHeader == null || !authHeader.toUpperCase().startsWith("BEARER ")) {
      throw new UnauthorizedException("Invalid Bearer token");
    }

    String token = authHeader.substring(7);
    String currentSessionId = jwtTokenUtil.extractSessionId(token);

    List<SessionMetadata> activeSessions = sessionRepository.findAllSessionsByUsername(username);
    activeSessions.stream()
        .filter(session -> !session.getSessionId().equals(currentSessionId))
        .forEach(session -> this.terminateSession(username, session.getSessionId()));
  }

  @Override
  public void terminateAllSessions(String username) {
    if (username == null) {
      throw new NullPointerException("Username cannot be null");
    }

    List<SessionMetadata> activeSessions = sessionRepository.findAllSessionsByUsername(username);
    activeSessions.forEach(
        session -> {
          try {
            this.terminateSession(username, session.getSessionId());
          } catch (Exception e) {
            log.error(
                "Failed to terminate session {} for user {}: {}",
                session.getSessionId(),
                username,
                e.getMessage(),
                e);
          }
        });
  }

  @Override
  @Scheduled(fixedRate = 86400000) // 1 day
  public void terminateExpiredSessions() {
    int itemCount = sessionRepository.deleteAllExpiredSessions();
    log.info("Terminated {} expired sessions", itemCount);
    // FIX: If `itemCount` is -1, it is error
    // TODO: Write to db (maybe)
  }

  @Override
  public void auditSession(String username, String sessionId) {
    // TODO: Implement
    log.info("username: {}, sessionId: {}", username, sessionId);
    //        throw new NotImplementedException();
  }
}
