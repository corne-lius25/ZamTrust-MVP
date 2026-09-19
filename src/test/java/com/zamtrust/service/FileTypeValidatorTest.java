package com.zamtrust.service;

import com.zamtrust.exception.InvalidFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileTypeValidator.
 * Resolves finding #10 (no MIME whitelist, stored XSS risk).
 */
class FileTypeValidatorTest {

    private FileTypeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FileTypeValidator();
    }

    @Test
    @DisplayName("Rejects HTML file")
    void rejectsHtml() {
        MockMultipartFile f = new MockMultipartFile(
                "file", "evil.html", "text/html",
                "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8));
        assertThrows(InvalidFileException.class, () -> validator.validate(f));
    }

    @Test
    @DisplayName("Rejects SVG file")
    void rejectsSvg() {
        MockMultipartFile f = new MockMultipartFile(
                "file", "evil.svg", "image/svg+xml",
                "<svg onload=\"alert(1)\"></svg>".getBytes(StandardCharsets.UTF_8));
        assertThrows(InvalidFileException.class, () -> validator.validate(f));
    }

    @Test
    @DisplayName("Rejects file with no extension")
    void rejectsNoExtension() {
        MockMultipartFile f = new MockMultipartFile(
                "file", "noext", "application/octet-stream",
                "hello".getBytes(StandardCharsets.UTF_8));
        assertThrows(InvalidFileException.class, () -> validator.validate(f));
    }

    @Test
    @DisplayName("Rejects ZIP content with .pdf extension (magic byte mismatch)")
    void rejectsSpoofedPdf() {
        byte[] zipMagic = {0x50, 0x4B, 0x03, 0x04, 0x00, 0x00, 0x00, 0x00};
        MockMultipartFile f = new MockMultipartFile(
                "file", "spoof.pdf", "application/pdf", zipMagic);
        assertThrows(InvalidFileException.class, () -> validator.validate(f));
    }

    @Test
    @DisplayName("Rejects PDF content declared as PNG")
    void rejectsWrongDeclaredType() {
        byte[] pdfMagic = {0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34};
        MockMultipartFile f = new MockMultipartFile(
                "file", "doc.png", "image/png", pdfMagic);
        assertThrows(InvalidFileException.class, () -> validator.validate(f));
    }

    @Test
    @DisplayName("Accepts plain text")
    void acceptsText() {
        MockMultipartFile f = new MockMultipartFile(
                "file", "note.txt", "text/plain",
                "legitimate".getBytes(StandardCharsets.UTF_8));
        assertDoesNotThrow(() -> validator.validate(f));
    }

    @Test
    @DisplayName("Accepts valid PNG (magic bytes correct)")
    void acceptsPng() {
        byte[] pngMagic = {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        MockMultipartFile f = new MockMultipartFile(
                "file", "image.png", "image/png", pngMagic);
        assertDoesNotThrow(() -> validator.validate(f));
    }

    @Test
    @DisplayName("Accepts valid PDF (magic bytes correct)")
    void acceptsPdf() {
        byte[] pdfMagic = {0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34};
        MockMultipartFile f = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", pdfMagic);
        assertDoesNotThrow(() -> validator.validate(f));
    }
}
