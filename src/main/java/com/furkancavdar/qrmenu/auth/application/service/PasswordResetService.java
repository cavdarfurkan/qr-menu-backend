package com.furkancavdar.qrmenu.auth.application.service;

import com.furkancavdar.qrmenu.auth.application.exception.InvalidPasswordResetTokenException;
import com.furkancavdar.qrmenu.auth.application.port.in.PasswordResetUseCase;
import com.furkancavdar.qrmenu.auth.application.port.in.SessionUseCase;
import com.furkancavdar.qrmenu.auth.application.port.out.PasswordResetRepositoryPort;
import com.furkancavdar.qrmenu.auth.application.port.out.UserRepositoryPort;
import com.furkancavdar.qrmenu.auth.domain.PasswordResetToken;
import com.furkancavdar.qrmenu.auth.domain.User;
import com.furkancavdar.qrmenu.auth.util.TokenHasher;
import com.furkancavdar.qrmenu.common.exception.ResourceNotFoundException;
import com.furkancavdar.qrmenu.common.port.MailPort;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService implements PasswordResetUseCase {

  @Value("${app.security.reset-token-expiry-minutes}")
  private String resetTokenExpiryMinutes;

  private final UserRepositoryPort userRepository;
  private final PasswordResetRepositoryPort passwordResetRepository;

  private final SessionUseCase sessionUseCase;

  private final PasswordEncoder passwordEncoder;
  private final MailPort mailPort;

  @Override
  public void requestReset(String email) {
    userRepository
        .findByEmail(email)
        .ifPresent(
            user -> {
              String rawToken = UUID.randomUUID().toString();
              String hashedToken = TokenHasher.sha256Hex(rawToken);

              passwordResetRepository.save(
                  new PasswordResetToken(
                      hashedToken,
                      user.getId(),
                      Instant.now()
                          .plus(Long.parseLong(resetTokenExpiryMinutes), ChronoUnit.MINUTES)));

              boolean isSent = mailPort.sendPasswordResetMail(email, rawToken);
              if (!isSent) {
                passwordResetRepository.delete(hashedToken);
              }
            });
  }

  @Override
  public boolean resetPassword(String rawToken, String newPassword) {
    String hashedToken = TokenHasher.sha256Hex(rawToken);

    PasswordResetToken resetToken =
        passwordResetRepository
            .findByToken(hashedToken)
            .orElseThrow(() -> new InvalidPasswordResetTokenException("Invalid or expired token"));

    if (resetToken.expiresAt().isBefore(Instant.now())) {
      passwordResetRepository.delete(hashedToken);
      throw new InvalidPasswordResetTokenException("Token has expired");
    }

    User user =
        userRepository
            .findById(resetToken.userId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    userRepository.updatePassword(user.getId(), passwordEncoder.encode(newPassword));
    passwordResetRepository.delete(hashedToken);
    sessionUseCase.terminateAllSessions(user.getUsername());

    return true;
  }

  @Override
  public boolean isExpired(String rawToken) {
    String hashedToken = TokenHasher.sha256Hex(rawToken);
    return passwordResetRepository.findByToken(hashedToken).isEmpty();
  }
}
