package com.furkancavdar.qrmenu.theme_module.adapter.api.dto.payload.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.furkancavdar.qrmenu.theme_module.domain.ThemeCategory;
import com.furkancavdar.qrmenu.theme_module.domain.ThemeManifest;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThemeResponseDto {
  private Long id;
  private String ownerUsername;
  private Boolean isFree;
  private ThemeCategory category;
  private String thumbnailUrl;
  private ThemeManifest themeManifest;
  private Map<String, JsonNode> themeSchemas;
  private Map<String, JsonNode> uiSchemas;
}
