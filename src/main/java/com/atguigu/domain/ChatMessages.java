package com.atguigu.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @Author pk
 * @Date 2026-10-01 19:24
 * @Description 实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("chat_messages")
public class ChatMessages {

    private String memoryId;
    /**
     * 聊天内容 json字符串格式
     */
    private String content;
}
