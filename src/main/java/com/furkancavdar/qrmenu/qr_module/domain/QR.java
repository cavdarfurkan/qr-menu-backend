package com.furkancavdar.qrmenu.qr_module.domain;

import com.furkancavdar.qrmenu.qr_module.domain.QRSize;

public record QR(Long id, String qrUrl, QRSize qrSize) {
}
