package com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.queue;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.furkancavdar.qrmenu.menu_module.domain.MenuJobType;
import lombok.Builder;
import lombok.ToString;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ToString
public class UnpublishMenuJobDto {
  @Builder.Default private MenuJobType type = MenuJobType.UNPUBLISH;

  private String siteName;

  private String statusUrl;

  private Long timestamp;
}
