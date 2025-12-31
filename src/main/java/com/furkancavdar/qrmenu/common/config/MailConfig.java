package com.furkancavdar.qrmenu.common.config;

import com.resend.Resend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfig {
  @Value("${resend.api.key}")
  private String resendApiKey;

  @Bean
  public Resend resend() {
    return new Resend(resendApiKey);
  }
}
