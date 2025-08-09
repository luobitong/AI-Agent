package com.luobt.aiagent.demo.invoke;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 仅用于测试获取 API Key
 */
@Component
public class TestApiKey {

    @Value("${spring.ai.dashscope.api-key}")
    private static String apiKey;

    public static String getApiKey() {
        return apiKey;
    }
}
