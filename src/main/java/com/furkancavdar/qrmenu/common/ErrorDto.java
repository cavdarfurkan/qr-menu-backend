package com.furkancavdar.qrmenu.common;

import java.util.List;
import lombok.Data;

@Data
public class ErrorDto {
  private int status;
  private List<String> errors;

  public void addError(String message) {
    this.errors.add(message);
  }
}
