package com.atguigu.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
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

    @Autowired
    private OpenAiEmbeddingModel openAiEmbeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder().id(memoryId) // 消息id
                .maxMessages(20) // 最大消息条数
                .chatMemoryStore(chatMemoryStoreConfig) // 持久化配置
                .build();
    }

    /**
     * 内存向量存储
     *
     * @return
     */
    @Bean
    public ContentRetriever contentRetrieverInMemory() {

        // 加载knowledge md文档，使用默认文档解析器解析
        Document hospitalDoc = loadFromClasspath("knowledge/hospital.md");
        Document departmentDoc = loadFromClasspath("knowledge/department.md");
        Document neuroDoc = loadFromClasspath("knowledge/neurology.md");
        List<Document> documents = Arrays.asList(hospitalDoc, departmentDoc, neuroDoc);
        // 使用内存向量存储
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();


        EmbeddingStoreIngestor embeddingStoreIngestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(openAiEmbeddingModel)
                .build();
        embeddingStoreIngestor.ingest(documents);
        // 从嵌入模型（EmbeddingStore）里检索和查询内容相关的信息
        return EmbeddingStoreContentRetriever.builder()
                .embeddingModel(openAiEmbeddingModel)
                .embeddingStore(embeddingStore)
                .minScore(0.5)
                .maxResults(3)
                .build();
    }

    /** 从 classpath 按流读取并解析，兼容 jar 包内运行 */
    private Document loadFromClasspath(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("classpath 资源未找到: " + path);
            }
            return new TextDocumentParser().parse(is);
        } catch (IOException e) {
            throw new RuntimeException("读取知识库文件失败: " + path, e);
        }
    }

    /**
     * pinecone向量存储
     *
     * @return
     */
    @Bean
    public ContentRetriever contentRetrieverInPinecone() {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore) // 向量存储数据库
                .embeddingModel(openAiEmbeddingModel) // 向量模型
                .minScore(0.6) // 最低得分
                .maxResults(3) // 最多返回结果数量
                .build();
    }
}
