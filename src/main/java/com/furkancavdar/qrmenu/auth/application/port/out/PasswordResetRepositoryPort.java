package com.furkancavdar.qrmenu.auth.application.port.out;

import com.furkancavdar.qrmenu.auth.domain.PasswordResetToken;
import java.util.Optional;

public interface PasswordResetRepositoryPort {
  void save(PasswordResetToken passwordResetToken);

  Optional<PasswordResetToken> findByToken(String token);

  void delete(String token);
}
