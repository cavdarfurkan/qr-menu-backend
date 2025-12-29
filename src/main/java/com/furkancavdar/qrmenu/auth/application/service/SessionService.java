package com.furkancavdar.qrmenu.auth.application.service;

import com.furkancavdar.qrmenu.auth.application.exception.SessionOwnerException;
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
      sessionRepository.deleteBySessionId(sessionId);
    } catch (Exception e) {
      log.error("Session {} termination failed: {}", sessionId, e.getMessage());
    }
  }

  @Override
  public void terminateAllOtherSessions(String username, String currentSessionId) {
    List<SessionMetadata> activeSessions = sessionRepository.findAllSessionsByUsername(username);
    activeSessions.stream()
        .filter(session -> !session.getSessionId().equals(currentSessionId))
        .forEach(session -> this.terminateSession(username, session.getSessionId()));
  }

  @Override
  @Scheduled(fixedRate = 86400000) // 1 day
  public void terminateExpiredSessions() {
    int itemCount = sessionRepository.deleteAllExpiredSessions();
    // TODO: Write to db (maybe)
  }

  @Override
  public void auditSession(String username, String sessionId) {
    // TODO: Implement
    log.info("username: {}, sessionId: {}", username, sessionId);
    //        throw new NotImplementedException();
  }
}
