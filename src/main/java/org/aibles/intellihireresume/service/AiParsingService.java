package org.aibles.intellihireresume.service;

public interface AiParsingService {
    void parse(String rawText, String resumeId);

    /**
     * Parse CV directly from file bytes — uses native file reading if the AI provider supports it
     * (e.g. Gemini inlineData), otherwise falls back to text extraction.
     */
    void parseFromFile(byte[] fileBytes, String mimeType, String resumeId);
}
