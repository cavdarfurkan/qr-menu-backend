package com.furkancavdar.qrmenu.menu_module.adapter.persistence.mapper;

import com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity.MenuContentEntity;
import com.furkancavdar.qrmenu.menu_module.domain.MenuContent;
import com.furkancavdar.qrmenu.theme_module.adapter.persistence.mapper.ThemeEntityMapper;

public class MenuContentEntityMapper {

    private MenuContentEntityMapper() {
    }

    public static MenuContentEntity toMenuContentEntity(MenuContent menuContent) {
        if (menuContent == null) {
            return null;
        }

        MenuContentEntity menuContentEntity = new MenuContentEntity();
        menuContentEntity.setId(menuContent.getId());
        menuContentEntity.setMenu(MenuEntityMapper.toMenuEntity(menuContent.getMenu()));
        menuContentEntity.setOwnerId(menuContent.getOwnerId());
        menuContentEntity.setTheme(ThemeEntityMapper.toThemeEntity(menuContent.getTheme()));
        menuContentEntity.setCollectionName(menuContent.getCollectionName());
        menuContentEntity.setContentJson(menuContent.getContent());
        return menuContentEntity;
    }

    public static MenuContent toMenuContent(MenuContentEntity menuContentEntity) {
        if (menuContentEntity == null) {
            return null;
        }

        return new MenuContent(
                menuContentEntity.getId(),
                MenuEntityMapper.toMenu(menuContentEntity.getMenu()),
                menuContentEntity.getOwnerId(),
                ThemeEntityMapper.toTheme(menuContentEntity.getTheme()),
                menuContentEntity.getCollectionName(),
                menuContentEntity.getContentJson()
        );
    }
}
