package com.furkancavdar.qrmenu.menu_module.adapter.persistence.mapper;

import com.furkancavdar.qrmenu.auth.adapter.persistence.mapper.UserEntityMapper;
import com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity.MenuEntity;
import com.furkancavdar.qrmenu.menu_module.domain.Menu;
import com.furkancavdar.qrmenu.theme_module.adapter.persistence.mapper.ThemeEntityMapper;

public class MenuEntityMapper {

  private MenuEntityMapper() {}

  public static MenuEntity toMenuEntity(Menu menu) {
    if (menu == null) {
      return null;
    }

    MenuEntity menuEntity = new MenuEntity();
    menuEntity.setId(menu.getId());
    menuEntity.setMenuName(menu.getMenuName());
    menuEntity.setOwner(UserEntityMapper.toEntity(menu.getOwner()));
    menuEntity.setSelectedTheme(ThemeEntityMapper.toThemeEntity(menu.getSelectedTheme()));
    return menuEntity;
  }

  public static Menu toMenu(MenuEntity menuEntity) {
    if (menuEntity == null) {
      return null;
    }

    return new Menu(
        menuEntity.getId(),
        menuEntity.getMenuName(),
        UserEntityMapper.toDomain(menuEntity.getOwner()),
        ThemeEntityMapper.toTheme(menuEntity.getSelectedTheme()));
  }
}
