package org.aibles.intellihireresume.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.aibles.intellihireresume.service.TextExtractionService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
@Service
public class TextExtractionServiceImpl implements TextExtractionService {

    @Override
    public String extractFromPdf(byte[] fileBytes) {
        log.info("Extracting text from PDF, size={} bytes", fileBytes.length);
        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("PDF text extraction complete, extracted {} chars", text.length());
            return text;
        } catch (IOException e) {
            log.error("Failed to extract text from PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract text from PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public String extractFromDocx(byte[] fileBytes) {
        log.info("Extracting text from DOCX, size={} bytes", fileBytes.length);
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(fileBytes));
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = extractor.getText();
            log.info("DOCX text extraction complete, extracted {} chars", text.length());
            return text;
        } catch (Exception e) {
            log.error("Failed to extract text from DOCX: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to extract text from DOCX: " + e.getMessage(), e);
        }
    }
}
