package com.luobt.aiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 自定义日志 Advisor
 * 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
 */
@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public int getOrder() {
		return 0;
	}

	private AdvisedRequest before(AdvisedRequest request) {
		log.info("request: {}", request.userText());
		return request;
	}

	private void observeAfter(AdvisedResponse advisedResponse) {
		log.info("response: {}", advisedResponse.adviseContext());
	}

	@Override
	public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
		advisedRequest = this.before(advisedRequest);
		AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
		this.observeAfter(advisedResponse);
		return advisedResponse;
	}

	@Override
	public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
		advisedRequest = this.before(advisedRequest);
		Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
		return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponses, this::observeAfter);
	}
}
