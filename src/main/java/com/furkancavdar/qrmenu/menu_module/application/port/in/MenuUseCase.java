package com.furkancavdar.qrmenu.menu_module.application.port.in;

import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.BuildMenuResultDto;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.MenuDto;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.UserMenuDto;
import java.util.List;

public interface MenuUseCase {
  void createMenu(MenuDto menuDto);

  /**
   * @param menuId ID of the menu
   * @param ownerName Username of the menu's owner
   * @param isAdmin {@code true} if {@link com.furkancavdar.qrmenu.auth.domain.User} has {@link
   *     com.furkancavdar.qrmenu.auth.domain.Role} {@code ADMIN},
   *     <p>otherwise {@code false}
   * @author Furkan Çavdar
   */
  void deleteMenu(Long menuId, String ownerName, Boolean isAdmin);

  BuildMenuResultDto buildMenu(Long menuId, String ownerName);

  void publishMenu();

  void unpublishMenu();

  List<UserMenuDto> allUserMenus(String ownerName);

  MenuDto getMenu(Long menuId, String ownerName);

  void updateMenu(Long menuId, MenuDto menuDto, String ownerName);

  boolean checkDomainAvailability(String domain);
}
