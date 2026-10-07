import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import com.atguigu.Assistant;
import com.atguigu.ChatMessages;
import com.atguigu.domain.Appointment;
import com.atguigu.service.AppointmentService;
import com.atguigu.agent.XiaozhiAgent;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenCountEstimator;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import com.atguigu.XiaozhiMedicalApplication;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Slf4j
@SpringBootTest(classes = XiaozhiMedicalApplication.class)
public class XiaozhiMedicalTests {


    @Test
    public void testOpenAI() {
        OpenAiChatModel model = OpenAiChatModel.builder().baseUrl("https://api.deepseek.com").apiKey("sk-35e9f42c26054acc986f5f3760e7bcdd").modelName("deepseek-flash").build();
        String s = model.chat("你好");
        log.info("result: {}", s);
    }


    @Test
    public void testOpenAIStreaming() throws InterruptedException {

        OpenAiStreamingChatModel streamingChatModel = OpenAiStreamingChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey("sk-35e9f42c26054acc986f5f3760e7bcdd")
                .modelName("deepseek-flash")
                .build();

        // 用 CountDownLatch 阻塞主线程，等流式真正结束再让测试返回
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

        streamingChatModel.chat("请介绍一下当前AI发展状况", new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                // 每次收到流式片段时触发（实时输出到控制台或前端）
                System.out.print(partialResponse);
                System.out.flush();
            }

            @Override
            public void onCompleteResponse(ChatResponse chatResponse) {
                System.out.println();
                latch.countDown(); // 释放主线程
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("流式输出出错: " + throwable.getMessage());
                throwable.printStackTrace();
                latch.countDown(); // 出错也要释放，避免测试一直卡住
            }
        });

        // 等待流式结束（最多等 60 秒）
        latch.await(60, java.util.concurrent.TimeUnit.SECONDS);
    }



    @Test
    public void testOllama() {
        OllamaChatModel ollamaChatModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("deepseek-r1:1.5b")
                .maxRetries(3)
                .logRequests(true)
                .logResponses(true)
                .temperature(0.3)
                .think(true)
                .returnThinking(true)
                .timeout(Duration.ofSeconds(120))
                .build();
        ChatResponse re = ollamaChatModel.chat(UserMessage.from("中国目前AI发展现状"));
        System.out.println(re.aiMessage().text());
    }

    @Test
    public void testOllamaStreaming() throws InterruptedException {
        StreamingChatModel ollamaStreaming = OllamaStreamingChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("deepseek-r1:1.5b")
                .timeout(Duration.ofSeconds(120))
                .think(true)
                .temperature(0.3)
                .returnThinking(true)
                .logRequests(true)
                .logResponses(true)
                .build();
        // 用 CountDownLatch 阻塞主线程，等流式真正结束再让测试返回
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        ollamaStreaming.chat("目前中国AI发展现状", new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                System.out.print(partialResponse);
                System.out.flush();
            }

            @Override
            public void onCompleteResponse(ChatResponse chatResponse) {
                System.out.println();
                latch.countDown(); // 释放主线程
            }

            @Override
            public void onError(Throwable throwable) {
                log.error(throwable.getMessage());
            }
        });
        // 等待流式结束（最多等 120 秒）
        latch.await(120, java.util.concurrent.TimeUnit.SECONDS);
    }

    @Autowired
    private Assistant assistant;

    @Test
    public void testAiService() {
        dev.langchain4j.data.message.AiMessage s = assistant.chat("你好");
        System.out.println(s.text());
    }

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @Test
    public void testChatMemory() {
        UserMessage u1 = UserMessage.from("我是大宝");
        ChatResponse res = ollamaChatModel.chat(u1);
        AiMessage a1 = res.aiMessage();
        log.info(a1.text());
        ChatResponse cr = ollamaChatModel.chat(Arrays.asList(u1, a1, UserMessage.from("我是谁")));
        AiMessage cr1 = cr.aiMessage();
        log.info(cr1.text());
    }


    @Test
    public void testChatMemory2() {
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10).build();
        Assistant ass = AiServices.builder(Assistant.class)
                .chatMemory(messageWindowChatMemory)
                .chatModel(ollamaChatModel)
                .build();
        AiMessage a1 = ass.chat("我是哈喽");
        log.info(a1.text());
        AiMessage a2 = ass.chat("我是谁？");
        log.info(a2.text());
    }

    @Test
    public void testChatMemory3() {

        AiMessage a1 = assistant.chat("我是小庞同学");
        log.info(a1.text());
        AiMessage a2 = assistant.chat("我是谁？");
        log.info(a2.text());
    }

    @Test
    public void testChatMemory4() {

        AiMessage a1 = assistant.chat("1","我是哈喽同学");
        log.info(a1.text());
        AiMessage a2 = assistant.chat("1", "我是谁？");
        log.info(a2.text());
        AiMessage a3 = assistant.chat("2","我是谁？");
        log.info(a3.text());
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void testMongoDBInsert() {
        mongoTemplate.insert(new ChatMessages( "测试"));
    }

    @Test
    public void testMongoIns() {
        ChatMessages chat = new ChatMessages("历史列表2");
        mongoTemplate.insert(chat);
    }

    @Test
    public void testFindById() {
        ChatMessages chat = mongoTemplate.findById("6ab481d8c819a020a8f2381d", ChatMessages.class);
        System.out.println(chat);
    }

    @Test
    public void testUpdate() {
        Criteria criteria = Criteria.where("_id").is("6ab481d8c819a020a8f2381d");
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("content", "哈哈哈");

        mongoTemplate.upsert(query, update, ChatMessages.class);
    }

    @Test
    public void testUpsert() {
        Criteria criteria = Criteria.where("_id").is("6ab481d8c819a020a8f2381d");
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("content", "aoaoaoao");

        UpdateResult upsert = mongoTemplate.upsert(query, update, ChatMessages.class);
        System.out.println(upsert);
    }

    @Test
    public void testDelete() {
        Criteria criteria = Criteria.where("_id").is("6ab481f9b1f39f29e97cad8d");
        Query query = new Query(criteria);
        DeleteResult result = mongoTemplate.remove(query, ChatMessages.class);
        System.out.println(result);
    }

    @Test
    public void testChat() {
        /*AiMessage chat = assistant.chat("3", "你好");
        System.out.println(chat);*/
        /*AiMessage cha = assistant.chat("3", "我刚才问了什么");
        System.out.println(cha);*/

        AiMessage chat = assistant.chat("你好");
        System.out.println(chat);
    }

    @Autowired
    private XiaozhiAgent XiaozhiAgent;

    @Test
    public void testTool() {
        String res = XiaozhiAgent.chat("12345", "1+2是多少");
        System.out.println(res);
    }

    @Autowired
    private AppointmentService appointmentService;
    @Test
    void testGetOne() {
        Appointment appointment = new Appointment();
        appointment.setUsername("张三");
        appointment.setIdCard("123456789012345678");
        appointment.setDepartment("内科");
        appointment.setDate("2025-04-14");
        appointment.setTime("上午");
        Appointment appointmentDB = appointmentService.getOne(appointment);
        System.out.println(appointmentDB);
    }

    @Test
    void testSave() {
        Appointment appointment = new Appointment();
        appointment.setUsername("张三");
        appointment.setIdCard("123456789012345678");
        appointment.setDepartment("内科");
        appointment.setDate("2025-04-14");
        appointment.setTime("上午");
        appointment.setDoctorName("张医生");
        appointmentService.save(appointment);
    }
    @Test
    void testRemoveById() {
        appointmentService.removeById(1L);
    }

    @Test
    public void testDocumentLoad() {
        Document document = ClassPathDocumentLoader.loadDocument("./new.pdf", new ApachePdfBoxDocumentParser());
        System.out.println(document);

        /*List<Document> docs = ClassPathDocumentLoader.loadDocuments("ab", new TextDocumentParser());
        docs.stream().forEach(System.out::println);*/

        /*List<Document> docs = ClassPathDocumentLoader.loadDocumentsRecursively("ab", new TextDocumentParser());
        docs.stream().forEach(System.out::println);*/


        /*Document document = FileSystemDocumentLoader.loadDocument("E:\\xiaozhi-medical\\src\\main\\resources\\xiaozhi-prompt-template.txt");
        System.out.println(document);*/
    }

    @Test
    public void testEmbedding() {
        InMemoryEmbeddingStore<TextSegment> inMemoryEmbeddingStore = new InMemoryEmbeddingStore<>();
        AllMiniLmL6V2EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        TextSegment s1 = TextSegment.from("I like bootball.");
        Embedding e1 = embeddingModel.embed(s1).content();
        inMemoryEmbeddingStore.add(e1, s1);

        TextSegment s2 = TextSegment.from("The weather is good day.");
        Embedding e2 = embeddingModel.embed(s2).content();
        inMemoryEmbeddingStore.add(e2, s2);

        Embedding searchEmbedding = embeddingModel.embed("what is your favourite sport?").content();
        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder().queryEmbedding(searchEmbedding).maxResults(1).build();


        List<EmbeddingMatch<TextSegment>> matches = inMemoryEmbeddingStore.search(embeddingSearchRequest).matches();
        EmbeddingMatch<TextSegment> embeddingMatch = matches.get(0);
        System.out.println("score: " + embeddingMatch.score() + ", text: " + embeddingMatch.embedded().text());

        String jsonStr = inMemoryEmbeddingStore.serializeToJson();
        System.out.println("jsonStr: " + jsonStr);
        InMemoryEmbeddingStore<TextSegment> textSegment = inMemoryEmbeddingStore.fromJson(jsonStr);
        System.out.println("textSegment: " + textSegment);

        System.out.println("=========================");

        inMemoryEmbeddingStore.serializeToFile("memoryEmbedding.store");
        InMemoryEmbeddingStore<TextSegment> t = inMemoryEmbeddingStore.fromFile("memoryEmbedding.store");

    }


    @Test
    public void testToken() {
        String text = "这是一个测试文本，为了计算token数量";
        UserMessage userMessage = UserMessage.from(text);
        HuggingFaceTokenCountEstimator huggingFaceTokenCountEstimator = new HuggingFaceTokenCountEstimator();
        int i1 = huggingFaceTokenCountEstimator.estimateTokenCountInText(text);
        System.out.println("token长度：" + i1);
        int i2 = huggingFaceTokenCountEstimator.estimateTokenCountInMessage(userMessage);
        System.out.println("token长度：" + i2);
    }

    @Autowired
    private OpenAiEmbeddingModel openAiEmbeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> pineconeEmbeddingStore;

    @Test
    public void testVector() {
        /*Response<Embedding> embed = openAiEmbeddingModel.embed("你好，测试嵌入模型");
        log.info("vector: {}", embed.content().vector());
        log.info("vector length: {}", embed.content().vector().length);

        String uuid = embeddingStore.add(embed.content());
        System.out.println(uuid);*/

        pineconeEmbeddingStore.remove("8cf807d7-5023-4cf6-a65a-1cffd1536e81");
    }

    @Test
    public void testPineconeStore() {
        TextSegment t1 = TextSegment.from("我喜欢篮球");
        Embedding e1 = openAiEmbeddingModel.embed(t1).content();
        pineconeEmbeddingStore.add(e1, t1);

        TextSegment t2 = TextSegment.from("我一般早上不吃早饭");
        Embedding e2 = openAiEmbeddingModel.embed(t2).content();
        pineconeEmbeddingStore.add(e2, t2);

        Embedding searchEmbedding = openAiEmbeddingModel.embed("你喜欢什么运动").content();
        EmbeddingSearchResult<TextSegment> embeddingSearchResult = pineconeEmbeddingStore.search(EmbeddingSearchRequest.builder()
                .query("你喜欢什么运动")
                .queryEmbedding(searchEmbedding)
//                .minScore(0.8D)
                .maxResults(1)
                .build());
        List<EmbeddingMatch<TextSegment>> matches = embeddingSearchResult.matches();
        EmbeddingMatch<TextSegment> match = matches.get(0);
        System.out.println("score: " + match.score() + ", embedded: " + match.embedded().text() + ", embeddedId: " + match.embeddingId());
    }

    /**
     * 将本当上传到pinecone向量库中
     */
    @Test
    public void uploadDocsToPinecone() {
        Document d1 = FileSystemDocumentLoader.loadDocument("E:\\xiaozhi-medical\\src\\main\\resources\\knowledge\\医院信息.md");
        Document d2 = FileSystemDocumentLoader.loadDocument("E:\\xiaozhi-medical\\src\\main\\resources\\knowledge\\科室信息.md");
        Document d3 = FileSystemDocumentLoader.loadDocument("E:\\xiaozhi-medical\\src\\main\\resources\\knowledge\\神经内科.md");

        // 文本向量化
        EmbeddingStoreIngestor.builder()
                .embeddingStore(pineconeEmbeddingStore)
                .embeddingModel(openAiEmbeddingModel)
                .build()
                .ingest(Arrays.asList(d1, d2, d3));
    }

}