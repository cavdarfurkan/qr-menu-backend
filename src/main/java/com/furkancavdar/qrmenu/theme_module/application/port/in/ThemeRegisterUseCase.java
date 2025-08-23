package com.furkancavdar.qrmenu.theme_module.application.port.in;

import com.furkancavdar.qrmenu.theme_module.application.port.in.dto.ThemeDto;
import com.furkancavdar.qrmenu.theme_module.application.port.in.dto.ThemeManifestResultDto;
import com.furkancavdar.qrmenu.theme_module.application.port.in.dto.ThemeSchemasResultDto;
import org.springframework.data.domain.Page;

import java.io.InputStream;
import java.util.List;

public interface ThemeRegisterUseCase {
    void registerTheme(InputStream themeZipIs, InputStream previewImageIs, ThemeDto themeDto);

    /**
     * @param themeId   ID of the theme
     * @param ownerName Username of the theme's owner
     * @param isAdmin   {@code true} if {@link com.furkancavdar.qrmenu.auth.domain.User}
     *                  has {@link com.furkancavdar.qrmenu.auth.domain.Role} {@code ADMIN},
     *                  <p>
     *                  otherwise {@code false}
     * @author Furkan Çavdar
     * @see com.furkancavdar.qrmenu.auth.domain.User
     * @see com.furkancavdar.qrmenu.auth.domain.Role
     * @see com.furkancavdar.qrmenu.auth.adapter.persistence.entity.UserEntity
     * @see com.furkancavdar.qrmenu.auth.adapter.persistence.entity.RoleEntity
     */
    void unregisterTheme(Long themeId, String ownerName, Boolean isAdmin);

    ThemeManifestResultDto getManifest(Long themeId);

    ThemeSchemasResultDto getSchemas(Long themeId, List<String> refs, boolean includeUiSchemaFlag);

    Page<ThemeDto> getAllThemes(Integer page, Integer size);
}
