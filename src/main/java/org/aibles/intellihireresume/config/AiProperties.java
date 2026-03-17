package org.aibles.intellihireresume.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private String provider = "openai";
    private int maxRetries = 3;
    private OpenAiConfig openai = new OpenAiConfig();
    private ClaudeConfig claude = new ClaudeConfig();
    private GeminiConfig gemini = new GeminiConfig();
    private GrokConfig grok = new GrokConfig();
    private OpenRouterConfig openrouter = new OpenRouterConfig();

    @Data
    public static class OpenAiConfig {
        private String apiKey = "";
        private String model = "gpt-4o-mini";
        private int timeoutSeconds = 30;
    }

    @Data
    public static class ClaudeConfig {
        private String apiKey = "";
        private String model = "claude-haiku-4-5-20251001";
        private int timeoutSeconds = 30;
    }

    @Data
    public static class GeminiConfig {
        private String apiKey = "";
        private String model = "gemini-2.0-flash";
        private int timeoutSeconds = 30;
    }

    @Data
    public static class GrokConfig {
        private String apiKey = "";
        private String model = "grok-3";
        private int timeoutSeconds = 30;
    }

    @Data
    public static class OpenRouterConfig {
        private String apiKey = "";
        private String model = "google/gemini-2.0-flash";
        private int timeoutSeconds = 30;
        private String siteUrl = "https://intellihire.dev";
        private String siteName = "IntelliHire";
    }
}
