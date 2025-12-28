package com.furkancavdar.qrmenu.menu_module.domain;

public enum MenuJobStatus {
  PENDING("PENDING"),
  PROCESSING("PROCESSING"),
  DONE("DONE"),
  FAILED("FAILED");

  MenuJobStatus(String status) {}
}
