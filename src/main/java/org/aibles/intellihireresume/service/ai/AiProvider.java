package org.aibles.intellihireresume.service.ai;

import org.aibles.intellihireresume.dto.ai.ResumeParseResult;

import java.util.List;

public interface AiProvider {

    /**
     * Parse raw CV text and extract structured data using an AI provider.
     *
     * @param rawText      extracted text content of the CV
     * @param skillCatalog list of canonical skill names the AI must match against
     * @return structured parse result
     * @throws org.aibles.intellihireresume.exception.AiAuthException on 401 (no retry)
     * @throws RuntimeException on transient errors (caller handles retry)
     */
    ResumeParseResult parse(String rawText, List<String> skillCatalog);
}
