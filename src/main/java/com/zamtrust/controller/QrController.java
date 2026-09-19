package com.zamtrust.controller;

import com.zamtrust.service.QrService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/qr")
public class QrController {

    private final QrService qrService;

    public QrController(QrService qrService) {
        this.qrService = qrService;
    }

    @GetMapping(value = "/{verificationId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qr(@PathVariable String verificationId) {
        return ResponseEntity.ok(qrService.generateQrPng(verificationId, 300));
    }
}