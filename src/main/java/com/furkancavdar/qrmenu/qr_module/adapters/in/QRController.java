package com.furkancavdar.qrmenu.qr_module.adapters.in;

import com.furkancavdar.qrmenu.qr_module.application.service.GenerateQRService;
import jakarta.websocket.server.PathParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/qr")
public class QRController {
    private final GenerateQRService generateQRService;

    public QRController(GenerateQRService generateQRService) {
        this.generateQRService = generateQRService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(@PathParam("foo") String foo) {
        return new ResponseEntity<>(foo, HttpStatus.OK);
    }

//    @PostMapping
//    public GenerateQRResponse generate(@RequestBody GenerateQRRequest generateQRRequest) {
//        QR qr = generateQRService.generateQR(generateQRRequest);
//        return new GenerateQRResponse(qr.qrUrl());
//    }
}
