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

import java.util.List;
import java.util.Map;

/**
 * OpenRouter provider — routes to any model via OpenAI-compatible API.
 * Base URL: https://openrouter.ai/api/v1
 * Model examples: "google/gemini-2.0-flash", "meta-llama/llama-3.3-70b-instruct", "anthropic/claude-3.5-haiku"
 */
@Slf4j
public class OpenRouterProvider implements AiProvider {

    private static final String BASE_URL = "https://openrouter.ai/api/v1";

    private final AiProperties.OpenRouterConfig config;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenRouterProvider(AiProperties.OpenRouterConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        int timeoutMs = config.getTimeoutSeconds() * 1000;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);

        this.restClient = RestClient.builder()
            .baseUrl(BASE_URL)
            .requestFactory(factory)
            .defaultHeader("Authorization", "Bearer " + config.getApiKey())
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("HTTP-Referer", config.getSiteUrl())
            .defaultHeader("X-Title", config.getSiteName())
            .build();
    }

    @Override
    public ResumeParseResult parse(String rawText, List<String> skillCatalog) {
        String catalogStr = String.join(", ", skillCatalog);
        String systemPrompt = buildSystemPrompt(catalogStr);

        Map<String, Object> requestBody = Map.of(
            "model", config.getModel(),
            "response_format", Map.of("type", "json_object"),
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", rawText)
            )
        );

        try {
            String requestJson = objectMapper.writeValueAsString(requestBody);
            log.debug("Calling OpenRouter model={}", config.getModel());

            String responseJson = restClient.post()
                .uri("/chat/completions")
                .body(requestJson)
                .retrieve()
                .onStatus(status -> status.value() == 401,
                    (req, resp) -> { throw new AiAuthException(); })
                .onStatus(status -> status.value() == 429, (req, resp) -> {
                    String retryAfter = resp.getHeaders().getFirst("Retry-After");
                    long waitSeconds = retryAfter != null ? Long.parseLong(retryAfter) : 60L;
                    throw new AiRateLimitException(waitSeconds);
                })
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    (req, resp) -> { throw new RuntimeException("OpenRouter API error: " + resp.getStatusCode()); })
                .body(String.class);

            // OpenAI-compatible response: choices[0].message.content
            Map<?, ?> response = objectMapper.readValue(responseJson, Map.class);
            List<?> choices = (List<?>) response.get("choices");
            Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
            String content = (String) message.get("content");

            return objectMapper.readValue(content, ResumeParseResult.class);

        } catch (AiAuthException e) {
            throw e;
        } catch (AiRateLimitException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("OpenRouter API call failed: " + e.getMessage(), e);
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
