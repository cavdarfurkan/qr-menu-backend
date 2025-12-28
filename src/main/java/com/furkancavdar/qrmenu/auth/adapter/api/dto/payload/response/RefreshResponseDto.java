package com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshResponseDto {
  //    @JsonUnwrapped
  //    private JwtTokenPair jwtTokenPair;
  private String accessToken;
}
