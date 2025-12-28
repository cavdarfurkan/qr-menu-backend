package com.furkancavdar.qrmenu.qr_module.application.port.in.mapper;

import com.furkancavdar.qrmenu.qr_module.application.port.in.dto.QRDto;
import com.furkancavdar.qrmenu.qr_module.domain.QR;

/** QRDtoMapper */
public class QRDtoMapper {
  private QRDtoMapper() {}

  public static QRDto toDto(QR qr) {
    if (qr == null) {
      return null;
    }

    QRDto qrDto = new QRDto();
    qrDto.setId(qr.id());
    qrDto.setQrUrl(qr.qrUrl());
    if (qr.qrSize() != null) {
      qrDto.setQrSize(qr.qrSize());
    }
    return qrDto;
  }
}
