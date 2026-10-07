package com.atguigu.controller;

import com.atguigu.domain.ChatMessages;
import com.atguigu.agent.XiaozhiAgent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @Author pk
 * @Date 2026-10-01 19:24
 */
@Tag(name = "小智医疗", description = "小智医疗")
@RestController
@RequestMapping("xiaozhi")
public class XiaozhiController {

    private final XiaozhiAgent XiaozhiAgent;

    public XiaozhiController(XiaozhiAgent XiaozhiAgent) {
        this.XiaozhiAgent = XiaozhiAgent;
    }

    @Operation(summary = "对话", description = "对话")
    @PostMapping("/chat")
    public String chat(@RequestBody ChatMessages chatMessages) {
        return XiaozhiAgent.chat(chatMessages.getMemoryId(), chatMessages.getContent());
    }

    @Operation(summary = "流式对话", description = "流式对话")
    @PostMapping(value = "/streamingChat")
    public Flux<String> streamingChat(@RequestBody ChatMessages chatMessages) {
        return XiaozhiAgent.streamingChat(chatMessages.getMemoryId(), chatMessages.getContent());
    }
}
