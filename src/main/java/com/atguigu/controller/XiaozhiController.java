package com.atguigu.controller;

import com.atguigu.domain.ChatMessages;
import com.atguigu.service.XiaozhiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author pk
 * @Date 2026-10-01 19:24
 */
@Tag(name = "小智医疗", description = "小智医疗")
@RestController
@RequestMapping("xiaozhi")
public class XiaozhiController {

    private final XiaozhiService xiaozhiService;

    public XiaozhiController(XiaozhiService xiaozhiService) {
        this.xiaozhiService = xiaozhiService;
    }

    @Operation(summary = "对话", description = "对话")
    @PostMapping("/chat")
    public String chat(@RequestBody ChatMessages chatMessages) {
        return xiaozhiService.chat(chatMessages.getMemoryId(), chatMessages.getContent());
    }
}
