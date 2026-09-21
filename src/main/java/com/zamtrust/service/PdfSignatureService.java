package com.zamtrust.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.zamtrust.domain.SignaturePlacement;
import com.zamtrust.exception.CryptoException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Bakes a visible signature onto a PDF.
 *
 * Draws:
 *   - the signature image (user's saved PNG)
 *   - a text block underneath (signer name, role, timestamp, verification ID)
 *   - a small QR code linking to the public verification URL
 *
 * Coordinates in SignaturePlacement are fractions of page dimensions with
 * origin at the top-left (like a browser). PDF coordinates have origin at the
 * bottom-left. We convert both.
 *
 * Resolves Phase 1's visible-signature requirement.
 */
@Service
public class PdfSignatureService {

    private static final Logger log = LoggerFactory.getLogger(PdfSignatureService.class);
    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC);

    /**
     * Produces a signed PDF at `outputPath`.
     *
     * @param originalPdfPath   source PDF
     * @param signaturePngPath  signature PNG
     * @param placement         fractions of page (page 1-based, x/y from top-left)
     * @param signerName        e.g. "Corne M."
     * @param signerRole        e.g. "CEO"
     * @param signerOrg         e.g. "ZamTrust Ltd"
     * @param verificationId    e.g. "ZT-2026-A3F9C812B5D7"
     * @param verificationUrl   e.g. "https://zamtrust.dev/v/ZT-2026-A3F9C812B5D7"
     * @param signedAt          the moment the signature was applied
     * @param outputPath        where to write the signed PDF
     */
    public void applyVisibleSignature(Path originalPdfPath,
                                      Path signaturePngPath,
                                      SignaturePlacement placement,
                                      String signerName,
                                      String signerRole,
                                      String signerOrg,
                                      String verificationId,
                                      String verificationUrl,
                                      Instant signedAt,
                                      Path outputPath) {

        placement.validate();

        if (!Files.exists(originalPdfPath)) {
            throw new CryptoException("Original PDF not found: " + originalPdfPath);
        }
        if (!Files.exists(signaturePngPath)) {
            throw new CryptoException("Signature image not found: " + signaturePngPath);
        }

        // Verify the file is actually a PDF before attempting to parse it.
        // PDFBox throws an obscure "End-of-File, expected line at offset X"
        // error on non-PDF files, which is unhelpful to the caller.
        try {
            byte[] head = new byte[5];
            try (var in = Files.newInputStream(originalPdfPath)) {
                int read = in.read(head);
                if (read < 5
                        || head[0] != 0x25   // %
                        || head[1] != 0x50   // P
                        || head[2] != 0x44   // D
                        || head[3] != 0x46   // F
                        || head[4] != 0x2D)  // -
                {
                    throw new com.zamtrust.exception.NotAPdfException(
                            "The document is not a PDF. Only PDF files can be signed "
                            + "with a visible signature. Upload a PDF to continue.");
                }
            }
        } catch (java.io.IOException e) {
            throw new CryptoException("Could not read document file: " + e.getMessage(), e);
        }

        try (PDDocument doc = Loader.loadPDF(originalPdfPath.toFile())) {
            int pageIndex = placement.getPage() - 1;
            if (pageIndex < 0 || pageIndex >= doc.getNumberOfPages()) {
                throw new CryptoException("Page " + placement.getPage() + " out of range (PDF has "
                        + doc.getNumberOfPages() + " pages)");
            }

            PDPage page = doc.getPage(pageIndex);
            PDRectangle mediaBox = page.getMediaBox();
            float pageWidth = mediaBox.getWidth();
            float pageHeight = mediaBox.getHeight();

            // Convert placement fractions (top-left origin) → PDF coordinates (bottom-left origin)
            float boxWidth = (float) (placement.getWidth() * pageWidth);
            float boxHeight = (float) (placement.getHeight() * pageHeight);
            float boxX = (float) (placement.getX() * pageWidth) + mediaBox.getLowerLeftX();
            float boxYTop = pageHeight - (float) (placement.getY() * pageHeight) + mediaBox.getLowerLeftY();
            float boxY = boxYTop - boxHeight;

            // Signature image: 65% of the box height (top portion)
            float imageHeight = boxHeight * 0.62f;
            float imageWidth = boxWidth;
            float imageY = boxY + (boxHeight - imageHeight);

            // Draw signature image
            PDImageXObject sigImage = PDImageXObject.createFromFile(
                    signaturePngPath.toAbsolutePath().toString(), doc);

            try (PDPageContentStream cs = new PDPageContentStream(
                    doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

                // Signature image
                cs.drawImage(sigImage, boxX, imageY, imageWidth, imageHeight);

                // Thin underline
                cs.setStrokingColor(new java.awt.Color(180, 180, 180));
                cs.setLineWidth(0.5f);
                cs.moveTo(boxX, imageY - 2);
                cs.lineTo(boxX + boxWidth, imageY - 2);
                cs.stroke();

                // Text block below the image
                float textStartY = imageY - 6;
                float lineHeight = 9f;

                PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                PDType1Font fontMono = new PDType1Font(Standard14Fonts.FontName.COURIER);

                // Name + role
                String nameLine = signerName != null ? signerName : "Unknown signer";
                if (signerRole != null && !signerRole.isBlank()) nameLine += " · " + signerRole;
                drawText(cs, fontBold, 8f, boxX, textStartY, nameLine);

                // Organization
                if (signerOrg != null && !signerOrg.isBlank()) {
                    drawText(cs, fontRegular, 7f, boxX, textStartY - lineHeight, signerOrg);
                }

                // Timestamp
                drawText(cs, fontRegular, 7f, boxX, textStartY - lineHeight * 2,
                        "Signed: " + TS_FMT.format(signedAt));

                // Verification ID (mono)
                drawText(cs, fontMono, 6.5f, boxX, textStartY - lineHeight * 3,
                        "ID: " + verificationId);

                // QR code in the bottom-right corner of the text block
                float qrSize = Math.min(boxHeight * 0.35f, 60f);
                float qrX = boxX + boxWidth - qrSize;
                float qrY = boxY + 2;
                PDImageXObject qr = generateQrImage(doc, verificationUrl, 200);
                cs.drawImage(qr, qrX, qrY, qrSize, qrSize);
            }

            Files.createDirectories(outputPath.getParent());
            doc.save(outputPath.toFile());
            log.info("[PDF-SIGN] Signed PDF written to {}", outputPath);

        } catch (IOException e) {
            throw new CryptoException("PDF signing failed: " + e.getMessage(), e);
        }
    }

    private void drawText(PDPageContentStream cs,
                          PDType1Font font,
                          float size,
                          float x,
                          float y,
                          String text) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(sanitize(text));
        cs.endText();
    }

    /** Strip characters PDFBox's standard fonts can't encode. */
    private String sanitize(String text) {
        if (text == null) return "";
        return text.replaceAll("[^\\x20-\\x7E]", "?");
    }

    /** Builds a QR code as a PDImageXObject. */
    private PDImageXObject generateQrImage(PDDocument doc, String url, int sizePx) throws IOException {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, sizePx, sizePx);
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", png);

            BufferedImage img = ImageIO.read(new java.io.ByteArrayInputStream(png.toByteArray()));
            return org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
                    .createFromImage(doc, img);
        } catch (Exception e) {
            throw new IOException("QR generation failed", e);
        }
    }
}
