package com.zamtrust.service;

import com.zamtrust.exception.InvalidFileException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * Validates uploaded files against a whitelist of MIME types and extensions,
 * and verifies actual file content via magic bytes.
 * Resolves finding #10.
 */
@Component
public class FileTypeValidator {

    private static final Map<String, Set<String>> ALLOWED = Map.of(
            "pdf",  Set.of("application/pdf"),
            "png",  Set.of("image/png"),
            "jpg",  Set.of("image/jpeg"),
            "jpeg", Set.of("image/jpeg"),
            "docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            "xlsx", Set.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            "txt",  Set.of("text/plain")
    );

    private static final Map<String, byte[][]> MAGIC = Map.of(
            "pdf",  new byte[][]{ {0x25, 0x50, 0x44, 0x46} },
            "png",  new byte[][]{ {(byte)0x89, 0x50, 0x4E, 0x47} },
            "jpg",  new byte[][]{ {(byte)0xFF, (byte)0xD8, (byte)0xFF} },
            "jpeg", new byte[][]{ {(byte)0xFF, (byte)0xD8, (byte)0xFF} },
            "zip",  new byte[][]{ {0x50, 0x4B, 0x03, 0x04} }
    );

    public void validate(MultipartFile file) {
        String ext = extensionOf(file.getOriginalFilename());
        if (ext == null || !ALLOWED.containsKey(ext)) {
            throw new InvalidFileException(
                    "File type not allowed. Accepted: PDF, PNG, JPG, DOCX, XLSX, TXT.");
        }

        String declaredType = file.getContentType();
        if (declaredType == null || !ALLOWED.get(ext).contains(declaredType)) {
            throw new InvalidFileException(
                    "The declared content type does not match the file extension.");
        }

        if (ext.equals("txt")) return;
        if (ext.equals("docx") || ext.equals("xlsx")) {
            verifyMagicBytes(file, "zip");
            return;
        }
        verifyMagicBytes(file, ext);
    }

    private void verifyMagicBytes(MultipartFile file, String ext) {
        byte[][] signatures = MAGIC.get(ext);
        if (signatures == null) return;

        byte[] head = new byte[8];
        try (InputStream in = file.getInputStream()) {
            int read = in.read(head);
            if (read < 4) {
                throw new InvalidFileException("File content is too short or unreadable.");
            }
        } catch (IOException e) {
            throw new InvalidFileException("Could not read file contents.");
        }

        for (byte[] sig : signatures) {
            boolean match = true;
            for (int i = 0; i < sig.length; i++) {
                if (head[i] != sig[i]) { match = false; break; }
            }
            if (match) return;
        }
        throw new InvalidFileException(
                "File content does not match the declared type (magic byte check failed).");
    }

    private String extensionOf(String filename) {
        if (filename == null) return null;
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return null;
        return filename.substring(dot + 1).toLowerCase();
    }
}
