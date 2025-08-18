package com.furkancavdar.qrmenu.theme_module.adapter.api.dto.payload.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.furkancavdar.qrmenu.theme_module.domain.ThemeManifest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ThemeManifestResponseDto {
    @JsonUnwrapped
    private ThemeManifest themeManifest;
}
