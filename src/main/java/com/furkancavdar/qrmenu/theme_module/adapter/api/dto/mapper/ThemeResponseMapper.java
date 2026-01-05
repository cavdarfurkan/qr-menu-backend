package com.furkancavdar.qrmenu.theme_module.adapter.api.dto.mapper;

import com.furkancavdar.qrmenu.theme_module.adapter.api.dto.payload.response.ThemeResponseDto;
import com.furkancavdar.qrmenu.theme_module.application.port.in.dto.ThemeDto;

public class ThemeResponseMapper {

  private ThemeResponseMapper() {}

  public static ThemeResponseDto fromThemeDto(ThemeDto themeDto) {
    if (themeDto == null) {
      return null;
    }

    return new ThemeResponseDto(
        themeDto.getId(),
        themeDto.getOwner() != null ? themeDto.getOwner().getUsername() : null,
        themeDto.getIsFree(),
        themeDto.getCategory(),
        themeDto.getThumbnailUrl(),
        themeDto.getThemeManifest(),
        themeDto.getThemeSchemas(),
        themeDto.getUiSchemas());
  }
}
