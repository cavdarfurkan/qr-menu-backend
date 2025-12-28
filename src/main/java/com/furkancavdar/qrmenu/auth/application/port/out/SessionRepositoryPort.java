package com.furkancavdar.qrmenu.auth.application.port.out;

import com.furkancavdar.qrmenu.auth.domain.SessionMetadata;
import java.util.List;
import java.util.Optional;

public interface SessionRepositoryPort {

  /**
   * Persist the {@link SessionMetadata}
   *
   * @param username Username of the user
   * @param sessionMetadata {@link SessionMetadata} object containing information about the session
   * @author Furkan Çavdar
   * @see SessionMetadata
   * @see com.furkancavdar.qrmenu.auth.application.port.in.dto.SessionMetadataDto
   * @since 1.0.0
   */
  void saveSession(String username, SessionMetadata sessionMetadata);

  /**
   * @param sessionId ID representing a session
   * @return {@code Optional<SessionMetadata>} Optional {@link SessionMetadata}
   * @author Furkan Çavdar
   * @see SessionMetadata
   * @see com.furkancavdar.qrmenu.auth.application.port.in.dto.SessionMetadataDto
   * @since 1.0.0
   */
  Optional<SessionMetadata> findBySessionId(String sessionId);

  /**
   * @param username Username of the user
   * @return {@code List<SessionMetadata>}
   * @author Furkan Çavdar
   * @see SessionMetadata
   * @since 1.0.0
   */
  List<SessionMetadata> findAllSessionsByUsername(String username);

  /**
   * @param sessionId ID representing a session
   * @author Furkan Çavdar
   * @since 1.0.0
   */
  void deleteBySessionId(String sessionId);

  /**
   * @param sessionId ID representing a session
   * @return {@code true} if session is expired, otherwise {@code false}
   * @author Furkan Çavdar
   * @since 1.0.0
   */
  boolean isSessionExpired(String sessionId);

  /**
   * @param username Username of the user
   * @param sessionId ID representing a session
   * @return {@code true} if user owns the session, otherwise {@code false}
   */
  boolean isUserOwnsSession(String username, String sessionId);
}
