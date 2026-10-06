package com.atguigu.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @Author pk
 * @Date memory provider配置
 * @Description memory provider配置
 */
@Component
public class ChatMemoryProviderConfig {

    @Autowired
    private ChatMemoryStoreConfig chatMemoryStoreConfig;

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder().id(memoryId) // 消息id
                .maxMessages(20) // 最大消息条数
                .chatMemoryStore(chatMemoryStoreConfig) // 持久化配置
                .build();
    }

    @Bean
    public ContentRetriever contentRetriever() {

        // 加载knowledge md文档，使用默认文档解析器解析
        Document hospitalDoc = ClassPathDocumentLoader.loadDocument("knowledge/医院信息.md", new TextDocumentParser());
        Document departmentDoc = ClassPathDocumentLoader.loadDocument("knowledge/科室信息.md", new TextDocumentParser());
        Document neuroDoc = ClassPathDocumentLoader.loadDocument("knowledge/神经内科.md", new TextDocumentParser());
        List<Document> documents = Arrays.asList(hospitalDoc, departmentDoc, neuroDoc);
        // 使用内存向量存储
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        // 使用默认的文档分割器
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);
        // 从嵌入模型（EmbeddingStore）里检索和查询内容相关的信息
        return EmbeddingStoreContentRetriever.from(embeddingStore);
    }
}
