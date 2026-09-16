package com.atguigu;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssistantChatMemoryConfig {

    @Bean
    public ChatMemory assistantChatMemory() {
        return MessageWindowChatMemory.builder().maxMessages(10).build();
    }
}
