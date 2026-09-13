import dev.langchain4j.model.openai.OpenAiAudioTranscriptionModelName;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.atguigu.XiaozhiMedicalApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
@Slf4j
@SpringBootTest(classes = XiaozhiMedicalApplication.class)
public class XiaozhiMedicalTests {


    @Test
    public void test() {
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .build();
        String s = model.chat("你好");
        log.info("result: {}", s);
    }
}
