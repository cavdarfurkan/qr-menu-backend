package com.furkancavdar.qrmenu.qr_module.application.port.in.dto;

import com.furkancavdar.qrmenu.qr_module.domain.QRSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRDto {
    private Long id;
    private String qrUrl;
    @Default
    private QRSize qrSize = new QRSize(128, 128);
}
