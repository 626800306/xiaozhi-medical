package com.atguigu;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "ollamaChatModel")
public interface Assistant {
    AiMessage chat(String userMessage);
}
