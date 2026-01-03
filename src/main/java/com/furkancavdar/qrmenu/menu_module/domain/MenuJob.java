package com.furkancavdar.qrmenu.menu_module.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class MenuJob {
  private String id;

  @Setter private MenuJobStatus status;

  private MenuJobType type;

  private Long timestamp;

  private Long menuId;

  public MenuJob(MenuJobStatus status) {
    this.status = status;
  }

  public MenuJob(String id, MenuJobStatus status) {
    this(status);
    this.id = id;
  }

  public MenuJob(String id, MenuJobStatus status, MenuJobType type, Long timestamp) {
    this.id = id;
    this.status = status;
    this.type = type;
    this.timestamp = timestamp;
  }

  public MenuJob(String id, MenuJobStatus status, MenuJobType type, Long timestamp, Long menuId) {
    this.id = id;
    this.status = status;
    this.type = type;
    this.timestamp = timestamp;
    this.menuId = menuId;
  }
}
