package com.furkancavdar.qrmenu.menu_module.adapter.api.dto.mapper;

import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.request.CreateMenuRequestDto;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.MenuDto;

public class CreateMenuRequestMapper {
  private CreateMenuRequestMapper() {}

  public static MenuDto toMenuDto(CreateMenuRequestDto dto, String ownerUsername) {
    if (dto == null) {
      return null;
    }

    return new MenuDto(
        null, dto.getMenuName(), ownerUsername, dto.getSelectedThemeId(), dto.getCustomDomain());
  }
}
