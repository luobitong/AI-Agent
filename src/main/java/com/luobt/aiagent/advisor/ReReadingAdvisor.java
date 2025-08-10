package com.luobt.aiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义 Re2 Advisor
 * 可提高大型语言模型的推理能力
 */
@Component
@Slf4j
public class ReReadingAdvisor implements CallAroundAdvisor , StreamAroundAdvisor {

    private AdvisedRequest beforeRequestHandler(AdvisedRequest advisedRequest){
        // 创建包含变量的模板
        String template = """
        {re2_input_query}
        Read the question again: {re2_input_query}
        """;

        // 使用 PromptTemplate 处理模板
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Map<String, Object> variables = new HashMap<>();
        variables.put("re2_input_query", advisedRequest.userText());

        String renderedText = promptTemplate.render(variables);

        log.info("renderedText:{}", renderedText);

        return AdvisedRequest.from(advisedRequest)
                .userText(renderedText) // 使用渲染后的文本
                .userParams(variables)
                .build();
    }


    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        advisedRequest = this.beforeRequestHandler(advisedRequest);
        return chain.nextAroundCall(advisedRequest);
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        advisedRequest = beforeRequestHandler(advisedRequest);
        return chain.nextAroundStream(advisedRequest);
    }

    @NotNull
    @Override
    public String getName() {

        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
