package com.furkancavdar.qrmenu.qr_module.application.port.out;

import com.furkancavdar.qrmenu.qr_module.domain.QR;

/** QRRepositoryPort */
public interface QRRepositoryPort {
  QRRepositoryPort save(QR qr);
}
