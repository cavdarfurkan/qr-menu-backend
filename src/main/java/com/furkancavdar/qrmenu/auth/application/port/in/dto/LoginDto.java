package com.furkancavdar.qrmenu.auth.application.port.in.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String ip;

    private String userAgentString;
}