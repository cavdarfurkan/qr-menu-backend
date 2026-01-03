package com.furkancavdar.qrmenu.menu_module.adapter.api.dto.mapper;

import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.request.UpdateMenuRequestDto;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.MenuDto;

public class UpdateMenuRequestMapper {
  private UpdateMenuRequestMapper() {}

  public static MenuDto toMenuDto(UpdateMenuRequestDto dto) {
    if (dto == null) {
      return null;
    }

    return new MenuDto(
        null, dto.getMenuName(), null, dto.getSelectedThemeId(), dto.getCustomDomain());
  }
}
