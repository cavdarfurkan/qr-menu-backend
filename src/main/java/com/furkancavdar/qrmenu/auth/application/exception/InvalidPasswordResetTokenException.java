package com.furkancavdar.qrmenu.auth.application.exception;

public class InvalidPasswordResetTokenException extends RuntimeException {
  public InvalidPasswordResetTokenException(String message) {
    super(message);
  }
}
