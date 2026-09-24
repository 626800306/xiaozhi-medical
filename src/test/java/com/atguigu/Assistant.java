package com.atguigu;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "ollamaChatModel",
//        chatMemory = "chatMemory",
        chatMemoryProvider = "chatMemoryProvider")
public interface Assistant {

    @SystemMessage("请用英文回答")
    @UserMessage("添加一些表情。{{userMessage}}")
    AiMessage chat(@V("userMessage") String userMessage);

    @SystemMessage("请用普通话回答问题")
    AiMessage chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
