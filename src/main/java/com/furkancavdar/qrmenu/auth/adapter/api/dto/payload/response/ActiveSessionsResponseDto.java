package com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.response;

import com.furkancavdar.qrmenu.auth.domain.SessionMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveSessionsResponseDto {
    private List<SessionMetadata> sessionMetadataList;
}
