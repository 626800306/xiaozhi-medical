package com.atguigu.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ChatMemoryProviderConfig {

    @Autowired
    private ChatMemoryStoreConfig chatMemoryStoreConfig;

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId) // 消息id
                .maxMessages(10) // 最大消息条数
                .chatMemoryStore(chatMemoryStoreConfig) // 持久化配置
                .build();
    }
}
