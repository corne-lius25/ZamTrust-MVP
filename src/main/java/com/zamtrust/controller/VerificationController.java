package com.zamtrust.controller;

import com.zamtrust.dto.VerificationResult;
import com.zamtrust.service.VerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/verifications")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping("/{verificationId}")
    public ResponseEntity<VerificationResult> verify(@PathVariable String verificationId) {
        return ResponseEntity.ok(verificationService.verifyByVerificationId(verificationId));
    }

    @PostMapping(value = "/{verificationId}/upload", consumes = "multipart/form-data")
    public ResponseEntity<VerificationResult> verifyUpload(
            @PathVariable String verificationId,
            @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(
                verificationService.verifyUpload(verificationId, file.getBytes()));
    }
}