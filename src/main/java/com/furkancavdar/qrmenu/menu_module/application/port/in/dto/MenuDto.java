package com.furkancavdar.qrmenu.menu_module.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuDto {
    private Long menuId;
    private String menuName;
    private String ownerUsername;
    private Long selectedThemeId;
}
