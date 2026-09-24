import com.atguigu.Assistant;
import com.atguigu.ChatMessages;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import com.atguigu.XiaozhiMedicalApplication;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Duration;
import java.util.Arrays;

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
}
