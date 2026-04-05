package com.english.langchain4j.aiService;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT, //手动装配
        chatModel = "openAiChatModel", // 绑定模型
        streamingChatModel = "openAiStreamingChatModel", // 绑定流式模型
//        chatMemory = "chatMemory" // 配置会话记忆对象
        chatMemoryProvider = "chatMemoryProvider", // 配置会话记忆对象->redis
//        contentRetriever = "contentRetriever", // 配置向量数据库检索对象 msg->rag->bigModel
        tools = "recommendTool"
)
//@AiService //自动装配
public interface IConsultService {

//    @SystemMessage("你是一个 AI 志愿填报顾问")
//    @UserMessage("{{it}}") // 拼接用户参数
    @SystemMessage(fromResource = "system_alpha1.txt")
    String chat(
           @MemoryId String memoryId,
           @UserMessage String message
    );
    
    /**
     * 流式对话方法
     * @param memoryId 会话 ID
     * @param message 用户消息
     * @return 流式响应
     */
    @SystemMessage(fromResource = "system_alpha1.txt")
    Flux<String> chatStream(
           @MemoryId String memoryId,
           @UserMessage String message
    );

}
