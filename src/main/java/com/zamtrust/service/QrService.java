package com.zamtrust.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class QrService {

    @Value("${zamtrust.base-url}")
    private String baseUrl;

    public byte[] generateQrPng(String verificationId, int size) {
        try {
            String content = baseUrl + "/verify/" + verificationId;
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("QR generation failed", e);
        }
    }

    public String verificationUrl(String verificationId) {
        return baseUrl + "/verify/" + verificationId;
    }
}