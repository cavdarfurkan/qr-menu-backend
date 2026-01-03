package com.furkancavdar.qrmenu.menu_module.application.port.in.mapper;

import com.furkancavdar.qrmenu.auth.domain.User;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.MenuDto;
import com.furkancavdar.qrmenu.menu_module.domain.Menu;
import com.furkancavdar.qrmenu.menu_module.util.DomainUtility;
import com.furkancavdar.qrmenu.theme_module.domain.Theme;

public class MenuDtoMapper {
  private MenuDtoMapper() {}

  public static MenuDto toMenuDto(Menu menu, String baseDomain) {
    if (menu == null) {
      return null;
    }

    MenuDto menuDto = new MenuDto();
    menuDto.setMenuId(menu.getId());
    menuDto.setMenuName(menu.getMenuName());
    menuDto.setOwnerUsername(menu.getOwner().getUsername());
    menuDto.setSelectedThemeId(menu.getSelectedTheme().getId());
    // Combine stored subdomain with base domain to return full domain
    if (menu.getCustomDomain() != null && !menu.getCustomDomain().isEmpty() && baseDomain != null) {
      menuDto.setCustomDomain(
          DomainUtility.combineSubdomainWithBase(menu.getCustomDomain(), baseDomain));
    } else {
      menuDto.setCustomDomain(menu.getCustomDomain());
    }
    return menuDto;
  }

  public static Menu toMenu(MenuDto menuDto, User owner, Theme selectedTheme) {
    if (menuDto == null) {
      return null;
    }

    return new Menu(
        menuDto.getMenuId(),
        menuDto.getMenuName(),
        owner,
        selectedTheme,
        menuDto.getCustomDomain());
  }
}
