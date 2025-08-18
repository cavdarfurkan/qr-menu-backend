package com.furkancavdar.qrmenu.qr_module.application.port.in.dto;

import java.util.Optional;

import com.furkancavdar.qrmenu.qr_module.domain.QRSize;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQRDto {
	@NotBlank
	private String url;

	@NotBlank
	private QRSize qrSize;
}
