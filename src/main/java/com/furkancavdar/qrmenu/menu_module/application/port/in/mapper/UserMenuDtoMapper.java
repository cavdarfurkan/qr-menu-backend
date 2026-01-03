package com.furkancavdar.qrmenu.menu_module.application.port.in.mapper;

import com.furkancavdar.qrmenu.auth.domain.User;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.UserMenuDto;
import com.furkancavdar.qrmenu.menu_module.domain.Menu;
import com.furkancavdar.qrmenu.theme_module.domain.Theme;

public class UserMenuDtoMapper {
  private UserMenuDtoMapper() {}

  public static UserMenuDto toUserMenuDto(Menu menu) {
    if (menu == null) {
      return null;
    }

    UserMenuDto userMenuDto = new UserMenuDto();
    userMenuDto.setMenuId(menu.getId());
    userMenuDto.setMenuName(menu.getMenuName());
    userMenuDto.setPublished(menu.getPublished());
    return userMenuDto;
  }

  public static Menu toMenu(UserMenuDto userMenuDto, User owner, Theme selectedTheme) {
    if (userMenuDto == null) {
      return null;
    }

    return new Menu(userMenuDto.getMenuName(), owner, selectedTheme);
  }
}
