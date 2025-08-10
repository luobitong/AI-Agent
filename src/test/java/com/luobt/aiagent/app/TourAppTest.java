package com.luobt.aiagent.app;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;


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
}