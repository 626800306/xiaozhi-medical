package com.atguigu.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeServerlessIndexConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PineconeEmbeddingStoreConfig {

    @Autowired
    private OpenAiEmbeddingModel openAiEmbeddingModel;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return PineconeEmbeddingStore.builder()
                .apiKey("pcsk_gWxwB_DyvFQsvyAxQEEY3temNYLokQyab8pbZxB3bdbNsnmADNk6wbDV4kjYWdG1iGSXC")
                .index("xiaozhi-medical") // 索引名称
                .nameSpace("xiaozhi-medical") // 命名空间
                .createIndex(PineconeServerlessIndexConfig.builder() // 索引在云服务器上配置
                        .cloud("AWS") // 云名称
                        .region("us-east-1") // 云区域
                        .dimension(openAiEmbeddingModel.dimension()) // 向量维度
                        .build())
                .build();

    }

}
