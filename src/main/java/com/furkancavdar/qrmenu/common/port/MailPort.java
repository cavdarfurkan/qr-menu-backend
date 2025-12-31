package com.furkancavdar.qrmenu.common.port;

public interface MailPort {
  /**
   * Sends a password reset email.
   *
   * @param to valid email address
   * @param token reset token
   * @return {@code true} if sent successfully, {@code false} otherwise
   * @throws IllegalArgumentException if parameters are null, empty, or invalid format
   */
  boolean sendPasswordResetMail(String to, String token);
}
