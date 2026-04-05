package com.english.langchain4j.aiService;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT, //手动装配
        chatModel = "openAiChatModel" // 绑定模型
)
public interface ISuggestService {

    @SystemMessage(fromResource = "suggest.txt")
    String suggest(String message);
}
