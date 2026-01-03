package com.furkancavdar.qrmenu.menu_module.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MenuJobType {
  BUILD("build"),
  UNPUBLISH("unpublish");

  private final String value;

  MenuJobType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
