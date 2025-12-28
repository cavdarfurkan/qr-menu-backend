package com.furkancavdar.qrmenu.menu_module.domain;

import com.furkancavdar.qrmenu.auth.domain.User;
import com.furkancavdar.qrmenu.theme_module.domain.Theme;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Menu {
  private Long id;
  private final String menuName;
  private final User owner;
  private final Theme selectedTheme;

  public Menu(String menuName, User owner, Theme selectedTheme) {
    this.menuName = menuName;
    this.owner = owner;
    this.selectedTheme = selectedTheme;
  }

  public Menu(Long id, String menuName, User owner, Theme selectedTheme) {
    this(menuName, owner, selectedTheme);
    this.id = id;
  }

  public Boolean isOwner(String username) {
    return owner.getUsername().equals(username);
  }
}
