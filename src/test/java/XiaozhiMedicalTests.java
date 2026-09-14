import com.atguigu.Assistant;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import com.atguigu.XiaozhiMedicalApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

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
}
