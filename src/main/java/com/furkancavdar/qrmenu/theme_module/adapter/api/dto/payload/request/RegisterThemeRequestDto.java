package com.furkancavdar.qrmenu.theme_module.adapter.api.dto.payload.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.furkancavdar.qrmenu.theme_module.domain.ThemeCategory;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RegisterThemeRequestDto {
  private Boolean isFree = Boolean.TRUE;

  @NotNull(message = "Category is required")
  private ThemeCategory category;
}
