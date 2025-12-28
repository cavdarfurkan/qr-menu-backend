package com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.queue;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.HydratedItemDto;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.ToString;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ToString
public class BuildMenuJobDto {
  private String themeLocationUrl;

  private String siteName;

  //    /**
  //     * key: collection
  //     * <p>
  //     * value: json payload
  //     */
  //    private Map<String, List<JsonNode>> contents;

  private Map<String, List<HydratedItemDto>> contents;

  private String statusUrl;

  private Long timestamp;
}
