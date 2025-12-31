package com.furkancavdar.qrmenu.auth.application.port.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"oldPassword", "newPassword"})
@EqualsAndHashCode
public class UpdatePasswordDto {
  @NotBlank
  @Size(min = 3, max = 50)
  private String username;

  @NotBlank
  @Size(min = 6, max = 40)
  private String oldPassword;

  @NotBlank
  @Size(min = 6, max = 40)
  private String newPassword;
}
