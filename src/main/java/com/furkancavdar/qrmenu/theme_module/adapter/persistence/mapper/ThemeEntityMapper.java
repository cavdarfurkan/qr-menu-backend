package com.furkancavdar.qrmenu.theme_module.adapter.persistence.mapper;

import com.furkancavdar.qrmenu.auth.adapter.persistence.mapper.UserEntityMapper;
import com.furkancavdar.qrmenu.theme_module.adapter.persistence.entity.ThemeEntity;
import com.furkancavdar.qrmenu.theme_module.domain.Theme;

public class ThemeEntityMapper {

  private ThemeEntityMapper() {}

  public static ThemeEntity toThemeEntity(Theme theme) {
    if (theme == null) {
      return null;
    }

    ThemeEntity themeEntity = new ThemeEntity();
    themeEntity.setId(theme.getId());
    themeEntity.setOwner(UserEntityMapper.toEntity(theme.getOwner()));
    themeEntity.setThumbnailUrl(theme.getThumbnailUrl());
    themeEntity.setThemeLocationUrl(theme.getThemeLocationUrl());
    themeEntity.setIsFree(theme.isFree());
    themeEntity.setCategory(theme.getCategory());
    themeEntity.setThemeManifest(theme.getThemeManifest());
    themeEntity.setThemeSchemas(theme.getThemeSchemas());
    themeEntity.setUiSchemas(theme.getUiSchemas());

    return themeEntity;
  }

  public static Theme toTheme(ThemeEntity themeEntity) {
    if (themeEntity == null) {
      return null;
    }

    return new Theme(
        themeEntity.getId(),
        UserEntityMapper.toDomain(themeEntity.getOwner()),
        themeEntity.getThumbnailUrl(),
        themeEntity.getThemeLocationUrl(),
        themeEntity.getIsFree(),
        themeEntity.getCategory(),
        themeEntity.getThemeManifest(),
        themeEntity.getThemeSchemas(),
        themeEntity.getUiSchemas());
  }
}
