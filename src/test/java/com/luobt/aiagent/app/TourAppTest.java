package com.luobt.aiagent.app;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;


@SpringBootTest
@Slf4j
class TourAppTest {

    @Resource
    private TourApp tourApp;
    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是美美";
        String answer = tourApp.doChat(message, chatId);
        // 第二轮
        message = "我想去北京旅游";
        answer = tourApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我想去哪里旅游？刚跟你说过，帮我回忆一下";
        answer = tourApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatByStream() throws InterruptedException {
        String chatId = UUID.randomUUID().toString();

        tourApp.doChatByStream("你好，我是美美", chatId)
                .doOnNext(chunk -> log.info("收到 chunk: {}", chunk))
                .doOnError(e -> log.error("流异常", e))
                .subscribe();

        Thread.sleep(Duration.ofSeconds(10).toMillis());
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是美美，我下周一个人想去北京旅游，预算只有2000，但我不知道该怎么做";
        TourApp.TourReport tourReport = tourApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(tourReport);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是美美，我下周一个人想去北京旅游，预算只有2000";
        String answer = tourApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
        testMessage("我想去深圳旅游，有哪些好玩的地方");

        // 测试网页抓取：旅游攻略分析
        testMessage("别人去深圳玩都去了哪里");

        // 测试资源下载：图片下载
        testMessage("直接下载一张适合做手机壁纸的深圳风景图片为文件");

        // 测试终端操作：执行代码
        testMessage("执行 Python3 脚本来生成数据分析报告");

        // 测试文件操作：保存用户档案
        testMessage("保存我的旅游攻略为文件");

        // 测试 PDF 生成
        testMessage("生成一份‘国庆深圳旅游攻略’PDF，包含外出游玩、宝藏美食和必买清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = tourApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }
}