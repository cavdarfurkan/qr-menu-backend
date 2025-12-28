package com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.response;

import com.furkancavdar.qrmenu.auth.domain.SessionMetadata;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveSessionsResponseDto {
  private List<SessionMetadata> sessionMetadataList;
}
