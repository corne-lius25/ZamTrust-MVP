package com.zamtrust.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health endpoints for operational visibility.
 * The /storage endpoint is safe to expose publicly — it reveals only
 * whether the app's directories are writable, not their contents.
 */
@RestController
@RequestMapping("/api/public/health")
public class HealthController {

    @Value("${zamtrust.storage.documents-dir}")
    private String documentsDir;

    @Value("${zamtrust.storage.keys-dir}")
    private String keysDir;

    @Value("${zamtrust.storage.signatures-dir:./storage/signatures}")
    private String signaturesDir;

    @GetMapping("/storage")
    public ResponseEntity<Map<String, Object>> storage() {
        Map<String, Object> result = new LinkedHashMap<>();
        boolean allOk = true;

        allOk &= check(result, "documents", documentsDir);
        allOk &= check(result, "keys", keysDir);
        allOk &= check(result, "signatures", signaturesDir);

        result.put("allWritable", allOk);
        return ResponseEntity.ok(result);
    }

    private boolean check(Map<String, Object> out, String label, String pathStr) {
        try {
            Path p = Paths.get(pathStr);
            boolean exists = Files.exists(p);
            boolean writable = exists && Files.isWritable(p);
            out.put(label, Map.of(
                    "path", pathStr,
                    "exists", exists,
                    "writable", writable
            ));
            return writable;
        } catch (Exception e) {
            out.put(label, Map.of("path", pathStr, "error", e.getMessage()));
            return false;
        }
    }
}
