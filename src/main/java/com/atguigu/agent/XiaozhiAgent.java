package com.atguigu.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

/**
 * @Author pk
 * @Date 2026-10-01 19:25
 * @Description agent接口
 */
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        streamingChatModel = "openAiStreamingChatModel", // 对话模型
        chatMemoryProvider = "chatMemoryProvider", // 对话模型提供者
        tools = "appointmentTools", // tools配置
//        contentRetriever = "contentRetrieverInMemory"  // 内存向量存储
        contentRetriever = "contentRetrieverInPinecone")  // pinecone向量存储
// 向量存储
public interface XiaozhiAgent {
    @SystemMessage(fromResource = "xiaozhi-prompt-template.txt")
    String chat(@MemoryId String memoryId, @UserMessage String userMessage);

    @SystemMessage(fromResource = "xiaozhi-prompt-template.txt")
    Flux<String> streamingChat(@MemoryId String memoryId, @UserMessage String userMessage);

}
