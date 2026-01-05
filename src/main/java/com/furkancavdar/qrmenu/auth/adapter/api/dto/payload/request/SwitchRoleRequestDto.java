package com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwitchRoleRequestDto {
  @NotNull(message = "activate field is required")
  private Boolean activate;
}
