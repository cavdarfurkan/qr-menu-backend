package com.furkancavdar.qrmenu.menu_module.adapter.persistence.mapper;

import com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity.MenuEntity;
import com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity.MenuJobEntity;
import com.furkancavdar.qrmenu.menu_module.domain.MenuJob;

public class MenuJobEntityMapper {

  private MenuJobEntityMapper() {}

  public static MenuJobEntity toMenuJobEntity(MenuJob menuJob) {
    if (menuJob == null) {
      return null;
    }

    MenuJobEntity menuJobEntity = new MenuJobEntity();
    menuJobEntity.setId(menuJob.getId());
    menuJobEntity.setMenuJobStatus(menuJob.getStatus());
    menuJobEntity.setType(menuJob.getType());
    menuJobEntity.setTimestamp(menuJob.getTimestamp());
    if (menuJob.getMenuId() != null) {
      MenuEntity menuEntity = new MenuEntity();
      menuEntity.setId(menuJob.getMenuId());
      menuJobEntity.setMenu(menuEntity);
    }
    return menuJobEntity;
  }

  public static MenuJob toMenuJob(MenuJobEntity menuJobEntity) {
    if (menuJobEntity == null) {
      return null;
    }

    Long menuId = menuJobEntity.getMenu() != null ? menuJobEntity.getMenu().getId() : null;
    return new MenuJob(
        menuJobEntity.getId(),
        menuJobEntity.getMenuJobStatus(),
        menuJobEntity.getType(),
        menuJobEntity.getTimestamp(),
        menuId);
  }
}
