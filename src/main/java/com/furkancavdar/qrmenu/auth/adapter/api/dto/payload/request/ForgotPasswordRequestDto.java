package com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ForgotPasswordRequestDto {
  @NotBlank(message = "Email cannot be blank")
  @Size(max = 100, message = "Email must not exceed 100 characters")
  @Email(message = "Email must be valid")
  private String email;
}
