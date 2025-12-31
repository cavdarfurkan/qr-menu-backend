package com.furkancavdar.qrmenu.auth.application.port.in;

import com.furkancavdar.qrmenu.auth.domain.SessionMetadata;
import java.util.List;

public interface SessionUseCase {

  /**
   * Method to list all active sessions for a user
   *
   * @param username Username of the user
   * @return List of the user's active sessions information {@link SessionMetadata}
   * @author Furkan Çavdar
   * @since 1.0.0
   */
  List<SessionMetadata> getActiveSessions(String username);

  /**
   * Method to terminate a specific session. (Log out from device)
   *
   * @param username Username of the user
   * @param sessionId Session of the user
   * @author Furkan Çavdar
   * @since 1.0.0
   */
  void terminateSession(String username, String sessionId);

  /**
   * Method to terminate all sessions except current (Log out from all other devices)
   *
   * @param username Username of the user
   * @param authHeader Authorization header for Bearer Token
   * @author Furkan Çavdar
   * @since 1.0.0
   */
  void terminateAllOtherSessions(String username, String authHeader);

  /**
   * Method to terminate all sessions
   *
   * @param username Username of the user
   * @author Furkan Çavdar
   * @since 1.0.0
   */
  void terminateAllSessions(String username);

  /**
   * Terminate all expired sessions
   *
   * @author Furkan Çavdar
   * @since 1.0.0
   */
  void terminateExpiredSessions();

  /**
   * Audit session trails, such as login, logout events
   *
   * @param username Username of the user
   * @param sessionId Session sessionId
   * @author Furkan Çavdar
   * @since 1.0.0
   */
  void auditSession(String username, String sessionId);
}
