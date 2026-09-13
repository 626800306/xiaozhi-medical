import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.atguigu.XiaozhiMedicalApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = XiaozhiMedicalApplication.class)
public class XiaozhiMedicalTests {


    @Test
    public void test() {
        OpenAiChatModel model = OpenAiChatModel.builder().baseUrl("https://api.deepseek.com").apiKey("sk-35e9f42c26054acc986f5f3760e7bcdd").modelName("deepseek-flash").build();
        String s = model.chat("你好");
        log.info("result: {}", s);
    }


    @Test
    public void test1() throws InterruptedException {

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
}
