package com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString(exclude = {"oldPassword", "newPassword"})
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdatePasswordRequestDto {
  @NotBlank
  @Size(min = 6, max = 40)
  private String oldPassword;

  @NotBlank
  @Size(min = 6, max = 40)
  private String newPassword;
}
