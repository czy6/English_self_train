package com.english.langchain4j.aiService;


import com.english.api.dto.ChatImageRequestDTO;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT, //手动装配
        chatModel = "openAiChatModel", // 绑定模型
        chatMemoryProvider = "chatMemoryProvider", // 配置会话记忆对象->redis
        tools = "chatWithImageTool"
)
public interface IChatWithImageService {

    @SystemMessage(fromResource = "image.txt")
    String chatWithImage(@MemoryId String memoryId,@UserMessage String message);
}
