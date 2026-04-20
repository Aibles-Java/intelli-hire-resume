package org.aibles.intellihireresume.service.impl;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TextExtractionServiceImplTest {

    private TextExtractionServiceImpl textExtractionService;

    @BeforeEach
    void setUp() {
        textExtractionService = new TextExtractionServiceImpl();
    }

    // ---- PDF Tests ----

    @Test
    void extractFromPdf_ShouldReturnText_WhenValidPdf() throws IOException {
        byte[] pdfBytes = createSamplePdf("John Doe\njohn@example.com\nJava Developer");

        String result = textExtractionService.extractFromPdf(pdfBytes);

        assertThat(result).isNotNull();
        assertThat(result).contains("John Doe");
    }

    @Test
    void extractFromPdf_ShouldThrowRuntimeException_WhenInvalidBytes() {
        byte[] invalidBytes = "not a pdf".getBytes();

        assertThatThrownBy(() -> textExtractionService.extractFromPdf(invalidBytes))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to extract text from PDF");
    }

    @Test
    void extractFromPdf_ShouldThrowRuntimeException_WhenEmptyBytes() {
        byte[] emptyBytes = new byte[0];

        assertThatThrownBy(() -> textExtractionService.extractFromPdf(emptyBytes))
                .isInstanceOf(RuntimeException.class);
    }

    // ---- DOCX Tests ----

    @Test
    void extractFromDocx_ShouldReturnText_WhenValidDocx() throws IOException {
        byte[] docxBytes = createSampleDocx("Jane Smith\njane@example.com\nSoftware Engineer");

        String result = textExtractionService.extractFromDocx(docxBytes);

        assertThat(result).isNotNull();
        assertThat(result).contains("Jane Smith");
    }

    @Test
    void extractFromDocx_ShouldThrowRuntimeException_WhenInvalidBytes() {
        byte[] invalidBytes = "not a docx".getBytes();

        assertThatThrownBy(() -> textExtractionService.extractFromDocx(invalidBytes))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to extract text from DOCX");
    }

    @Test
    void extractFromDocx_ShouldReturnEmptyText_WhenEmptyDocument() throws IOException {
        byte[] emptyDocxBytes = createSampleDocx("");

        String result = textExtractionService.extractFromDocx(emptyDocxBytes);

        assertThat(result).isNotNull();
    }

    // ---- Helpers ----

    private byte[] createSamplePdf(String text) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 700);
                for (String line : text.split("\\n")) {
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -15);
                }
                contentStream.endText();
            }

            document.save(out);
            return out.toByteArray();
        }
    }

    private byte[] createSampleDocx(String text) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (String line : text.split("\\n")) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(line);
            }
            document.write(out);
            return out.toByteArray();
        }
    }
}
