package com.furkancavdar.qrmenu.common.adapter;

import com.furkancavdar.qrmenu.common.port.MailPort;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResendMailAdapter implements MailPort {

  @Value("${app.frontend-url}")
  private String frontendUrl;

  @Value("${app.mail.from}")
  private String fromEmail;

  @Value("${app.security.reset-token-expiry-minutes}")
  private String resetTokenExpiryMinutes;

  private final Resend resend;
  private final SpringTemplateEngine templateEngine;

  @Override
  public boolean sendPasswordResetMail(String to, String token) {
    Context context = new Context();
    context.setVariable("email", to);
    context.setVariable("resetUrl", "%s/reset-password?token=%s".formatted(frontendUrl, token));
    context.setVariable("expiresMinutes", resetTokenExpiryMinutes);

    String html = templateEngine.process("mail/password-reset", context);

    CreateEmailOptions params =
        CreateEmailOptions.builder()
            .from(fromEmail)
            .to(to)
            .subject("Reset your password")
            .html(html)
            .build();

    try {
      CreateEmailResponse data = resend.emails().send(params);
      log.info("Password reset email sent to: {}", to);
      log.info(data.getId());
      return true;
    } catch (ResendException e) {
      log.error("Failed to send password reset email to {}: {}", to, e.getMessage(), e);
      return false;
    }
  }
}
