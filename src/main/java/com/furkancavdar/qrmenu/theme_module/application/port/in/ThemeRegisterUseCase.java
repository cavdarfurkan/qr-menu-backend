package com.furkancavdar.qrmenu.theme_module.application.port.in;

import com.furkancavdar.qrmenu.theme_module.application.port.in.dto.ThemeDto;
import com.furkancavdar.qrmenu.theme_module.application.port.in.dto.ThemeManifestResultDto;
import com.furkancavdar.qrmenu.theme_module.application.port.in.dto.ThemeSchemasResultDto;
import com.furkancavdar.qrmenu.theme_module.domain.ThemeCategory;
import java.io.InputStream;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ThemeRegisterUseCase {
  void registerTheme(InputStream themeZipIs, InputStream previewImageIs, ThemeDto themeDto);

  /**
   * @param themeId ID of the theme
   * @param ownerName Username of the theme's owner
   * @param isAdmin {@code true} if {@link com.furkancavdar.qrmenu.auth.domain.User} has {@link
   *     com.furkancavdar.qrmenu.auth.domain.Role} {@code ADMIN}, otherwise {@code false}
   * @param isDeveloper {@code true} if {@link com.furkancavdar.qrmenu.auth.domain.User} has {@link
   *     com.furkancavdar.qrmenu.auth.domain.Role} {@code DEVELOPER}, otherwise {@code false}
   * @author Furkan Çavdar
   * @see com.furkancavdar.qrmenu.auth.domain.User
   * @see com.furkancavdar.qrmenu.auth.domain.Role
   * @see com.furkancavdar.qrmenu.auth.adapter.persistence.entity.UserEntity
   * @see com.furkancavdar.qrmenu.auth.adapter.persistence.entity.RoleEntity
   */
  void unregisterTheme(Long themeId, String ownerName, Boolean isAdmin, Boolean isDeveloper);

  ThemeManifestResultDto getManifest(Long themeId);

  ThemeSchemasResultDto getSchemas(Long themeId, List<String> refs, boolean includeUiSchemaFlag);

  Page<ThemeDto> getAllThemes(Integer page, Integer size, ThemeCategory category);

  Page<ThemeDto> getThemesByOwner(
      String username, Integer page, Integer size, ThemeCategory category);
}
