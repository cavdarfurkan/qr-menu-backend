package com.furkancavdar.qrmenu.qr_module.application.service;

import org.springframework.stereotype.Service;

import com.furkancavdar.qrmenu.qr_module.application.port.in.GenerateQRUseCase;
import com.furkancavdar.qrmenu.qr_module.application.port.in.dto.GenerateQRDto;
import com.furkancavdar.qrmenu.qr_module.application.port.in.dto.QRDto;
import com.furkancavdar.qrmenu.qr_module.application.port.out.QRRepositoryPort;

import lombok.RequiredArgsConstructor;

/**
 * GenerateQRService
 */
@Service
@RequiredArgsConstructor
public class GenerateQRService implements GenerateQRUseCase {

//	private final QRRepositoryPort qrRepository;

	@Override
	public QRDto generateQR(GenerateQRDto generateQRDto) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'generateQR'");
	}

}
