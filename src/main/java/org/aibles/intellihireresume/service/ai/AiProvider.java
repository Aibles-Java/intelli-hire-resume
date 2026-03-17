package org.aibles.intellihireresume.service.ai;

import org.aibles.intellihireresume.dto.ai.ResumeParseResult;

import java.util.List;

public interface AiProvider {

    /**
     * Parse raw CV text and extract structured data using an AI provider.
     */
    ResumeParseResult parse(String rawText, List<String> skillCatalog);

    /**
     * Parse CV directly from file bytes (native multimodal support).
     * Override in providers that support file input (e.g. Gemini).
     * Default implementation throws UnsupportedOperationException.
     */
    default ResumeParseResult parseFromBytes(byte[] fileBytes, String mimeType, List<String> skillCatalog) {
        throw new UnsupportedOperationException("This AI provider does not support direct file parsing");
    }

    /**
     * Whether this provider can read raw file bytes directly (bypassing text extraction).
     */
    default boolean supportsFileInput() {
        return false;
    }
}
