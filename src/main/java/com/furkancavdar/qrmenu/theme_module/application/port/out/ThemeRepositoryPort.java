package com.furkancavdar.qrmenu.theme_module.application.port.out;

import com.furkancavdar.qrmenu.theme_module.domain.Theme;
import com.furkancavdar.qrmenu.theme_module.domain.ThemeCategory;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ThemeRepositoryPort {
  Theme save(Theme theme);

  Optional<Theme> findById(Long id);

  Optional<Theme> findByThemeName(String themeName);

  void deleteThemeById(Long id);

  Page<Theme> getAllThemes(Integer page, Integer size, ThemeCategory category);

  Page<Theme> findByOwnerUsername(
      String username, Integer page, Integer size, ThemeCategory category);
}
