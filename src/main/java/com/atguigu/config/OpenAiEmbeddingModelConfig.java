package com.atguigu.config;

import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiEmbeddingModelConfig {

    @Bean
    public OpenAiEmbeddingModel openAiEmbeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .baseUrl("https://api.openai-proxy.org/v1")
                .apiKey("sk-9xwWlnFYP3JiAc1MqGdn1Das8umM0pTAyOEqZY2hs75Xtu6s")
                .modelName("text-embedding-3-small")
                .build();
    }
}
