package com.furkancavdar.qrmenu.qr_module.application.port.in;

import com.furkancavdar.qrmenu.qr_module.application.port.in.dto.GenerateQRDto;
import com.furkancavdar.qrmenu.qr_module.application.port.in.dto.QRDto;

public interface GenerateQRUseCase {
	QRDto generateQR(GenerateQRDto generateQRDto);
}
