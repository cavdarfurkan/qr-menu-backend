package com.furkancavdar.qrmenu.theme_module.application.port.out;

import com.furkancavdar.qrmenu.theme_module.domain.Theme;

import java.io.InputStream;

public interface ThemeStoragePort {
    /**
     * Save/Store the theme zip file
     *
     * @param themeZipIs Theme zip input stream
     * @param themeName  Theme name
     * @return Stored/Saved location string
     * @author Furkan Çavdar
     * @since 1.0.0
     */
    String putTheme(InputStream themeZipIs, String themeName);

    String putThumbnail(InputStream thumbnailIs, String themeName);

    void deleteTheme(Theme theme);
}
