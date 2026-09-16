package com.atguigu;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "ollamaChatModel",
//        chatMemory = "chatMemory",
        chatMemoryProvider = "chatMemoryProvider")
public interface Assistant {

    AiMessage chat(String userMessage);

    AiMessage chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
