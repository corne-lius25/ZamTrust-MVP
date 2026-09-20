package com.zamtrust.controller;

import com.zamtrust.domain.SignatureImage;
import com.zamtrust.domain.User;
import com.zamtrust.exception.ResourceNotFoundException;
import com.zamtrust.repository.UserRepository;
import com.zamtrust.service.AuditService;
import com.zamtrust.service.SignatureImageService;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/signatures")
public class SignatureImageController {

    private final SignatureImageService signatureImageService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public SignatureImageController(SignatureImageService signatureImageService,
                                    UserRepository userRepository,
                                    AuditService auditService) {
        this.signatureImageService = signatureImageService;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    @GetMapping
    public ResponseEntity<List<SignatureDto>> list(Authentication auth) {
        User u = currentUser(auth);
        List<SignatureDto> result = signatureImageService.list(u.getId()).stream()
                .map(SignatureDto::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long id, Authentication auth) {
        User u = currentUser(auth);
        SignatureImage img = signatureImageService.require(id, u.getId());
        byte[] bytes = signatureImageService.readFile(img);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header("Cache-Control", "private, max-age=300")
                .body(bytes);
    }

    /**
     * Save a signature from base64 PNG data (used by the drawing/typing canvas).
     */
    @PostMapping(value = "/base64", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SignatureDto> saveBase64(@RequestBody SaveBase64Request req,
                                                   Authentication auth) {
        User u = currentUser(auth);
        SignatureImage saved = signatureImageService.saveFromBase64(
                u.getId(),
                req.label(),
                req.imageBase64(),
                req.widthPx() == null ? 300 : req.widthPx(),
                req.heightPx() == null ? 100 : req.heightPx(),
                Boolean.TRUE.equals(req.makeDefault())
        );
        auditService.log(u, "SIGNATURE_SAVED", "signature:" + saved.getId(), saved.getLabel(), null);
        return ResponseEntity.ok(SignatureDto.from(saved));
    }

    /**
     * Save a signature from a multipart PNG upload.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SignatureDto> saveUpload(@RequestParam("file") MultipartFile file,
                                                   @RequestParam(value = "label", required = false) String label,
                                                   @RequestParam(value = "widthPx", required = false) Integer widthPx,
                                                   @RequestParam(value = "heightPx", required = false) Integer heightPx,
                                                   @RequestParam(value = "makeDefault", required = false) Boolean makeDefault,
                                                   Authentication auth) {
        User u = currentUser(auth);
        SignatureImage saved = signatureImageService.saveFromMultipart(
                u.getId(),
                label,
                file,
                widthPx == null ? 300 : widthPx,
                heightPx == null ? 100 : heightPx,
                Boolean.TRUE.equals(makeDefault)
        );
        auditService.log(u, "SIGNATURE_SAVED", "signature:" + saved.getId(), saved.getLabel(), null);
        return ResponseEntity.ok(SignatureDto.from(saved));
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<?> setDefault(@PathVariable Long id, Authentication auth) {
        User u = currentUser(auth);
        signatureImageService.setDefault(id, u.getId());
        return ResponseEntity.ok(Map.of("message", "Default signature updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        User u = currentUser(auth);
        signatureImageService.delete(id, u.getId());
        auditService.log(u, "SIGNATURE_DELETED", "signature:" + id, null, null);
        return ResponseEntity.ok(Map.of("message", "Signature deleted"));
    }

    // ---------- DTOs ----------

    public record SignatureDto(
            Long id,
            String label,
            int widthPx,
            int heightPx,
            boolean isDefault,
            String sha256,
            String createdAt
    ) {
        static SignatureDto from(SignatureImage s) {
            return new SignatureDto(
                    s.getId(),
                    s.getLabel(),
                    s.getWidthPx(),
                    s.getHeightPx(),
                    s.isDefault(),
                    s.getSha256(),
                    s.getCreatedAt().toString()
            );
        }
    }

    public record SaveBase64Request(
            @Size(max = 100) String label,
            String imageBase64,
            Integer widthPx,
            Integer heightPx,
            Boolean makeDefault
    ) {}
}
