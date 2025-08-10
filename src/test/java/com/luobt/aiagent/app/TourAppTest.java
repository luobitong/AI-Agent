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
}