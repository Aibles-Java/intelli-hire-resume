package org.aibles.intellihireresume.service;

public interface TextExtractionService {

    String extractFromPdf(byte[] fileBytes);

    String extractFromDocx(byte[] fileBytes);
}
