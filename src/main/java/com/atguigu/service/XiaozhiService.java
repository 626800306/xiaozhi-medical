package com.atguigu.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

/**
 * @Author pk
 * @Date 2026-10-01 19:25
 * @Description agent接口
 */
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "ollamaChatModel", // 对话模型
        chatMemoryProvider = "chatMemoryProvider", // 对话模型提供者
        tools = "calculatorTools") // tools配置
public interface XiaozhiService {
    @SystemMessage(fromResource = "xiaozhi-prompt-template.txt")
    String chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
