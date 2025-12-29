package com.furkancavdar.qrmenu;

import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class QrMenuApplication {
  public static void main(String[] args) {
    SpringApplication.run(QrMenuApplication.class, args);
  }

  @Bean
  public JsonSchemaFactory jsonSchemaFactory() {
    return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void checkVirtualThreads() {
    log.info("Virtual threads enabled: {}", Thread.currentThread().isVirtual());
  }
}
