package com.furkancavdar.qrmenu.auth.domain;

import java.time.Instant;

public record PasswordResetToken(String token, Long userId, Instant expiresAt) {}
