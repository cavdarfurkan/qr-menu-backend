package com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.furkancavdar.qrmenu.menu_module.domain.MenuJobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MenuJobStatusResponse {
  private MenuJobStatus menuJobStatus;
}
