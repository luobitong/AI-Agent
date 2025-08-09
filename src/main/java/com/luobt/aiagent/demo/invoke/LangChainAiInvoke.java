package com.luobt.aiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

public class LangChainAiInvoke {

    public static void main(String[] args) {
        ChatLanguageModel qwenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.getApiKey())
                .modelName("qwen-max")
                .build();
        String answer = qwenChatModel.chat("你好，我是美美，你是谁");
        System.out.println(answer);
    }
}
