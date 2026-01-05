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
  private final String customDomain;
  private final Boolean published;
  private final Boolean isLatest;

  public Menu(String menuName, User owner, Theme selectedTheme) {
    this.menuName = menuName;
    this.owner = owner;
    this.selectedTheme = selectedTheme;
    this.customDomain = null;
    this.published = false;
    this.isLatest = true;
  }

  public Menu(String menuName, User owner, Theme selectedTheme, String customDomain) {
    this.menuName = menuName;
    this.owner = owner;
    this.selectedTheme = selectedTheme;
    this.customDomain = customDomain;
    this.published = false;
    this.isLatest = true;
  }

  public Menu(Long id, String menuName, User owner, Theme selectedTheme) {
    this(menuName, owner, selectedTheme);
    this.id = id;
  }

  public Menu(Long id, String menuName, User owner, Theme selectedTheme, String customDomain) {
    this(menuName, owner, selectedTheme, customDomain);
    this.id = id;
  }

  public Menu(
      Long id,
      String menuName,
      User owner,
      Theme selectedTheme,
      String customDomain,
      Boolean published) {
    this.menuName = menuName;
    this.owner = owner;
    this.selectedTheme = selectedTheme;
    this.customDomain = customDomain;
    this.published = published;
    this.id = id;
    this.isLatest = true;
  }

  public Menu(
      Long id,
      String menuName,
      User owner,
      Theme selectedTheme,
      String customDomain,
      Boolean published,
      Boolean isLatest) {
    this.menuName = menuName;
    this.owner = owner;
    this.selectedTheme = selectedTheme;
    this.customDomain = customDomain;
    this.published = published;
    this.id = id;
    this.isLatest = isLatest != null ? isLatest : true;
  }

  public Boolean isOwner(String username) {
    return owner.getUsername().equals(username);
  }
}
