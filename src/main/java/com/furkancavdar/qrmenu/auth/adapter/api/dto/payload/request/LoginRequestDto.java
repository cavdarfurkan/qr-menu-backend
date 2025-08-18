package com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
