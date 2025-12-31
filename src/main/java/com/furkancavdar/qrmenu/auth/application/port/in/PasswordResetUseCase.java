package com.furkancavdar.qrmenu.auth.application.port.in;

public interface PasswordResetUseCase {
  void requestReset(String email);

  boolean resetPassword(String rawToken, String newPassword);

  boolean isExpired(String rawToken);
}
