package com.luobt.aiagent.app;

import com.luobt.aiagent.advisor.MyLoggerAdvisor;
import com.luobt.aiagent.advisor.ReReadingAdvisor;
import com.luobt.aiagent.chatmemory.FileBasedChatMemory;
import com.luobt.aiagent.constant.FileConstant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;


import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class TourApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "你是一位专业的旅游规划师，具备丰富的目的地知识、行程设计经验和实用攻略储备。请根据用户提供的信息，定制详细且可行的旅游方案，需包含以下核心要素：\n" +
            "\n" +
            "基础信息整合：明确旅行时间（天数、季节）、预算范围（人均 / 总预算，含交通、住宿、餐饮、门票等细分）、出行人数及成员构成（如家庭带娃、情侣、朋友、独自旅行等）、特殊需求（如轮椅无障碍、素食、偏好小众景点等）。\n" +
            "目的地分析：结合用户偏好（自然景观 / 人文历史 / 美食探店 / 休闲度假 / 冒险体验等），推荐 1-3 个适配的目的地，并说明推荐理由（如季节适配性、与需求匹配度、性价比等）。\n" +
            "行程框架设计：按天数拆分每日行程，包含每日核心玩法、时间安排（如上午 / 下午 / 晚上的具体活动）、交通方式（当地打车 / 公交 / 租车等）、餐饮推荐（特色美食店或本地小吃）、住宿建议（区域位置、酒店类型）。\n" +
            "实用贴士：涵盖签证 / 签注要求（如适用）、必备物品清单（衣物、证件、药品等）、当地风俗禁忌、安全注意事项、预算控制技巧。\n" +
            "弹性方案：提供备选目的地、行程调整建议（如天气突变、体力不足时的替代方案）、紧急情况应对措施（如丢失证件、生病就医的流程）。\n" +
            "\n" +
            "请以清晰、有条理的结构呈现，语言通俗易懂，重点信息可突出标注，确保用户能快速理解并落地执行。";

    /**
     * 初始化 ChatClient
     *
     * @param dashscopeChatModel
     */
    public TourApp(ChatModel dashscopeChatModel) {
        // 初始化基于内存的对话记忆
//        ChatMemory chatMemory = new InMemoryChatMemory();
        // 初始化基于文件的对话记忆
        String fileDir = FileConstant.FILE_SAVE_DIR + "/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
//                        new SimpleLoggerAdvisor()
                        new MyLoggerAdvisor()
//                        new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 5))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * AI 基础对话（支持多轮对话记忆，SSE 流式传输）
     *
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 5))
                .stream()
                .content();
    }

    record TourReport(String title, String plan) {

    }

    /**
     * AI 旅游攻略功能（实战结构化输出）
     *
     * @param message
     * @param chatId
     * @return
     */
    public TourReport doChatWithReport(String message, String chatId) {
        TourReport tourReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成旅游攻略，标题为{用户名}的旅游攻略，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .call()
                .entity(TourReport.class);
        log.info("tourReport: {}", tourReport);
        return tourReport;
    }

    // AI 旅游知识库问答功能

    @Autowired
    @Qualifier("tourAppVectorStore")
    private VectorStore tourAppVectorStore;

    /**
     * 和 RAG 知识库进行对话
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {

        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用 RAG 知识库问答
                .advisors(new QuestionAnswerAdvisor(tourAppVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}
