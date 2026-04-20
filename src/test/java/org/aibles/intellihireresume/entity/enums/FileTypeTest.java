package org.aibles.intellihireresume.entity.enums;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FileTypeTest {

    @Test
    void fromExtension_ShouldReturnPDF_WhenExtensionIsPdf() {
        assertThat(FileType.fromExtension("pdf")).isEqualTo(FileType.PDF);
        assertThat(FileType.fromExtension("PDF")).isEqualTo(FileType.PDF);
        assertThat(FileType.fromExtension(".pdf")).isEqualTo(FileType.PDF);
    }

    @Test
    void fromExtension_ShouldReturnDOCX_WhenExtensionIsDocx() {
        assertThat(FileType.fromExtension("docx")).isEqualTo(FileType.DOCX);
        assertThat(FileType.fromExtension("DOCX")).isEqualTo(FileType.DOCX);
        assertThat(FileType.fromExtension(".docx")).isEqualTo(FileType.DOCX);
    }

    @Test
    void fromExtension_ShouldReturnNull_WhenExtensionIsNull() {
        assertThat(FileType.fromExtension(null)).isNull();
    }

    @Test
    void fromExtension_ShouldReturnNull_WhenExtensionIsUnknown() {
        assertThat(FileType.fromExtension("txt")).isNull();
        assertThat(FileType.fromExtension("xlsx")).isNull();
    }

    @Test
    void fromMimeType_ShouldReturnPDF_WhenMimeTypeIsApplicationPdf() {
        assertThat(FileType.fromMimeType("application/pdf")).isEqualTo(FileType.PDF);
    }

    @Test
    void fromMimeType_ShouldReturnDOCX_WhenMimeTypeIsDocx() {
        assertThat(FileType.fromMimeType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .isEqualTo(FileType.DOCX);
    }

    @Test
    void fromMimeType_ShouldReturnNull_WhenMimeTypeIsNull() {
        assertThat(FileType.fromMimeType(null)).isNull();
    }

    @Test
    void fromMimeType_ShouldReturnNull_WhenMimeTypeIsUnknown() {
        assertThat(FileType.fromMimeType("text/plain")).isNull();
    }

    @Test
    void isValidSize_ShouldReturnTrue_WhenSizeIsWithinLimit() {
        assertThat(FileType.PDF.isValidSize(1024L)).isTrue();
        assertThat(FileType.PDF.isValidSize(5 * 1024 * 1024L)).isTrue();
        assertThat(FileType.DOCX.isValidSize(100L)).isTrue();
    }

    @Test
    void isValidSize_ShouldReturnFalse_WhenSizeExceedsLimit() {
        assertThat(FileType.PDF.isValidSize(5 * 1024 * 1024L + 1)).isFalse();
        assertThat(FileType.DOCX.isValidSize(10 * 1024 * 1024L)).isFalse();
    }

    @Test
    void isValidSize_ShouldReturnFalse_WhenSizeIsZeroOrNegative() {
        assertThat(FileType.PDF.isValidSize(0L)).isFalse();
        assertThat(FileType.PDF.isValidSize(-1L)).isFalse();
    }

    @Test
    void getAllowedExtensions_ShouldContainPdfAndDocx() {
        Set<String> extensions = FileType.getAllowedExtensions();
        assertThat(extensions).contains("pdf", "docx");
        assertThat(extensions).hasSize(2);
    }

    @Test
    void getAllowedMimeTypes_ShouldContainBothMimeTypes() {
        Set<String> mimeTypes = FileType.getAllowedMimeTypes();
        assertThat(mimeTypes).contains(
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        assertThat(mimeTypes).hasSize(2);
    }

    @Test
    void getMimeType_ShouldReturnCorrectMimeType() {
        assertThat(FileType.PDF.getMimeType()).isEqualTo("application/pdf");
        assertThat(FileType.DOCX.getMimeType()).isEqualTo(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    @Test
    void getExtension_ShouldReturnCorrectExtension() {
        assertThat(FileType.PDF.getExtension()).isEqualTo("pdf");
        assertThat(FileType.DOCX.getExtension()).isEqualTo("docx");
    }

    @Test
    void getMaxSizeBytes_ShouldReturn5MB() {
        assertThat(FileType.PDF.getMaxSizeBytes()).isEqualTo(5 * 1024 * 1024L);
        assertThat(FileType.DOCX.getMaxSizeBytes()).isEqualTo(5 * 1024 * 1024L);
    }
}
