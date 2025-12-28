package com.furkancavdar.qrmenu.menu_module.adapter.persistence.mapper;

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
    return menuJobEntity;
  }

  public static MenuJob toMenuJob(MenuJobEntity menuJobEntity) {
    if (menuJobEntity == null) {
      return null;
    }

    return new MenuJob(menuJobEntity.getId(), menuJobEntity.getMenuJobStatus());
  }
}
