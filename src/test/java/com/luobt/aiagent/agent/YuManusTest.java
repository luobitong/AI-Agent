package com.luobt.aiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AIManusTest {

    @Resource
    private AIManus aiManus;

    @Test
    public void run() {
        String userPrompt = """
                我居住在上海静安区，请帮我找到 5 公里内合适的旅游外出地点，
                并结合一些网络图片，制定一份详细的旅游攻略，
                并以 PDF 格式输出""";
        String answer = aiManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}