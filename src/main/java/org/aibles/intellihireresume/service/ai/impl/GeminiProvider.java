package org.aibles.intellihireresume.service.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aibles.intellihireresume.config.AiProperties;
import org.aibles.intellihireresume.dto.ai.ResumeParseResult;
import org.aibles.intellihireresume.exception.AiAuthException;
import org.aibles.intellihireresume.exception.AiRateLimitException;
import org.aibles.intellihireresume.service.ai.AiProvider;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Google Gemini provider via REST API (generateContent endpoint).
 * API key is passed as a query param: ?key={apiKey}
 */
@Slf4j
public class GeminiProvider implements AiProvider {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";

    private final AiProperties.GeminiConfig config;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public GeminiProvider(AiProperties.GeminiConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();

        int timeoutMs = config.getTimeoutSeconds() * 1000;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);

        this.restClient = RestClient.builder()
            .baseUrl(BASE_URL)
            .requestFactory(factory)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    @Override
    public boolean supportsFileInput() {
        return true;
    }

    @Override
    public ResumeParseResult parseFromBytes(byte[] fileBytes, String mimeType, List<String> skillCatalog) {
        String catalogStr = String.join(", ", skillCatalog);
        String systemPrompt = buildSystemPrompt(catalogStr);
        String base64Data = Base64.getEncoder().encodeToString(fileBytes);

        Map<String, Object> requestBody = Map.of(
            "systemInstruction", Map.of(
                "parts", List.of(Map.of("text", systemPrompt))
            ),
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("inlineData", Map.of(
                        "mimeType", mimeType,
                        "data", base64Data
                    ))
                ))
            ),
            "generationConfig", Map.of(
                "response_mime_type", "application/json",
                "temperature", 0
            )
        );

        try {
            String requestJson = objectMapper.writeValueAsString(requestBody);
            log.debug("Calling Gemini (native file) model={}, mimeType={}", config.getModel(), mimeType);

            String uri = "/models/" + config.getModel() + ":generateContent?key=" + config.getApiKey();

            String responseJson = restClient.post()
                .uri(uri)
                .body(requestJson)
                .retrieve()
                .onStatus(status -> status.value() == 401 || status.value() == 403,
                    (req, resp) -> { throw new AiAuthException(); })
                .onStatus(status -> status.value() == 429, (req, resp) -> {
                    String retryAfter = resp.getHeaders().getFirst("Retry-After");
                    long waitSeconds = retryAfter != null ? Long.parseLong(retryAfter) : 60L;
                    throw new AiRateLimitException(waitSeconds);
                })
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    (req, resp) -> { throw new RuntimeException("Gemini API error: " + resp.getStatusCode()); })
                .body(String.class);

            Map<?, ?> response = objectMapper.readValue(responseJson, Map.class);
            List<?> candidates = (List<?>) response.get("candidates");
            Map<?, ?> content = (Map<?, ?>) ((Map<?, ?>) candidates.get(0)).get("content");
            List<?> parts = (List<?>) content.get("parts");
            String text = (String) ((Map<?, ?>) parts.get(0)).get("text");

            return objectMapper.readValue(text, ResumeParseResult.class);

        } catch (AiAuthException | AiRateLimitException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Gemini file parse failed: " + e.getMessage(), e);
        }
    }

    @Override
    public ResumeParseResult parse(String rawText, List<String> skillCatalog) {
        String catalogStr = String.join(", ", skillCatalog);
        String systemPrompt = buildSystemPrompt(catalogStr);

        // Gemini request body: systemInstruction + user content + JSON mime type
        Map<String, Object> requestBody = Map.of(
            "systemInstruction", Map.of(
                "parts", List.of(Map.of("text", systemPrompt))
            ),
            "contents", List.of(
                Map.of("parts", List.of(Map.of("text", rawText)))
            ),
            "generationConfig", Map.of(
                "response_mime_type", "application/json",
                "temperature", 0
            )
        );

        try {
            String requestJson = objectMapper.writeValueAsString(requestBody);
            log.debug("Calling Gemini model={}", config.getModel());

            String uri = "/models/" + config.getModel() + ":generateContent?key=" + config.getApiKey();

            String responseJson = restClient.post()
                .uri(uri)
                .body(requestJson)
                .retrieve()
                .onStatus(status -> status.value() == 401 || status.value() == 403,
                    (req, resp) -> { throw new AiAuthException(); })
                .onStatus(status -> status.value() == 429, (req, resp) -> {
                    String retryAfter = resp.getHeaders().getFirst("Retry-After");
                    long waitSeconds = retryAfter != null ? Long.parseLong(retryAfter) : 60L;
                    throw new AiRateLimitException(waitSeconds);
                })
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    (req, resp) -> { throw new RuntimeException("Gemini API error: " + resp.getStatusCode()); })
                .body(String.class);

            // Navigate: candidates[0].content.parts[0].text
            Map<?, ?> response = objectMapper.readValue(responseJson, Map.class);
            List<?> candidates = (List<?>) response.get("candidates");
            Map<?, ?> content = (Map<?, ?>) ((Map<?, ?>) candidates.get(0)).get("content");
            List<?> parts = (List<?>) content.get("parts");
            String text = (String) ((Map<?, ?>) parts.get(0)).get("text");

            return objectMapper.readValue(text, ResumeParseResult.class);

        } catch (AiAuthException | AiRateLimitException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Gemini API call failed: " + e.getMessage(), e);
        }
    }

    private String buildSystemPrompt(String catalogStr) {
        return """
            You are a professional CV/resume parser. Extract structured information from the resume text.

            Rules:
            1. Return ONLY valid JSON. No explanation, no markdown, no code fences.
            2. For skills, ONLY use names from this exact catalog (case-sensitive):
               [%s]
            3. If a field cannot be determined, use null.
            4. confidenceScore: 0.0 = skill mentioned once, 1.0 = primary skill with clear years of evidence.
            5. isPrimary = true for skills central to the candidate's career.
            6. summary: 2-4 sentences in English summarizing the candidate's profile.

            Required JSON structure:
            {
              "contact": {"fullName": "...", "email": "...", "phone": "...", "linkedinUrl": "...", "location": "..."},
              "summary": "...",
              "experiences": [{"company": "...", "title": "...", "startYear": 2020, "startMonth": 3, "endYear": 2023, "endMonth": 6, "isCurrent": false, "description": "..."}],
              "educations": [{"school": "...", "degree": "...", "field": "...", "startYear": 2016, "endYear": 2020, "description": "..."}],
              "skills": [{"name": "...", "confidenceScore": 0.9, "yearsExperience": 3.0, "isPrimary": true, "evidenceText": "..."}]
            }
            """.formatted(catalogStr);
    }
}
