package org.aibles.intellihireresume.config;

import org.aibles.intellihireresume.service.ai.AiProvider;
import org.aibles.intellihireresume.service.ai.impl.ClaudeProvider;
import org.aibles.intellihireresume.service.ai.impl.GeminiProvider;
import org.aibles.intellihireresume.service.ai.impl.GrokProvider;
import org.aibles.intellihireresume.service.ai.impl.OpenAiProvider;
import org.aibles.intellihireresume.service.ai.impl.OpenRouterProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "openai", matchIfMissing = true)
    public AiProvider openAiProvider(AiProperties props) {
        return new OpenAiProvider(props.getOpenai());
    }

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "claude")
    public AiProvider claudeProvider(AiProperties props) {
        return new ClaudeProvider(props.getClaude());
    }

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
    public AiProvider geminiProvider(AiProperties props) {
        return new GeminiProvider(props.getGemini());
    }

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "grok")
    public AiProvider grokProvider(AiProperties props) {
        return new GrokProvider(props.getGrok());
    }

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "openrouter")
    public AiProvider openRouterProvider(AiProperties props) {
        return new OpenRouterProvider(props.getOpenrouter());
    }
}
