package com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TerminateRequestDto {
  @NotBlank String sessionId;
}
