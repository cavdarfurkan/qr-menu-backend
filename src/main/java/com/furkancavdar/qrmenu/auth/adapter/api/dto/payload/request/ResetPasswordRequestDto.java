package com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString(exclude = {"newPassword"})
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResetPasswordRequestDto {
  @NotBlank private String token;

  @NotBlank
  @Size(min = 6, max = 40)
  private String newPassword;
}
