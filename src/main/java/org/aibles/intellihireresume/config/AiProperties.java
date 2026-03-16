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
}
