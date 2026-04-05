package com.english.ai.contoller;

import com.english.ai.domain.dto.ChatImageRequestDTO;
import com.english.ai.respository.ChatHistoryRespository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;
    private final ChatHistoryRespository chatHistoryRespository;

    @RequestMapping(value = "/chat")
    public String chat(String prompt, String chatId) {
        if (prompt == null || prompt.isEmpty()) {
            throw new IllegalArgumentException("prompt cannot be null or empty");
        }
        // 1.保存会话id
        chatHistoryRespository.save("chat", chatId);
        // 2.请求
        return chatClient.prompt()
                .messages(new UserMessage(prompt))
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .call()
                .content();
    }

    @RequestMapping(value = "/chat/common")
    public String commonChat(String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            throw new IllegalArgumentException("prompt cannot be null or empty");
        }
        // 请求
        return chatClient.prompt()
                .messages(new UserMessage(prompt))
                .call()
                .content();
    }

    @PostMapping("/chat/image")
    public String chatWithImage(@RequestBody ChatImageRequestDTO request) {
        // 1. 验证参数
        String prompt = request.getPrompt();
        String chatId = request.getChatId();
        List<String> imageUrls = request.getImageUrls();

        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("提示文本不能为空");
        }
        if (imageUrls == null || imageUrls.isEmpty()) {
            throw new IllegalArgumentException("至少需要上传一张图片");
        }

        // 2. 保存会话ID
        chatHistoryRespository.save("chat", chatId);

        // 3.组装 Spring AI 的多模态 UserMessage
        List<Media> mediaList = imageUrls.stream()
                .map(url -> Media.builder()
                        .mimeType(MimeTypeUtils.IMAGE_JPEG)
                        .data(URI.create(url))
                        .build())
                .toList();

        UserMessage userMessage = UserMessage.builder()
                .text(prompt)
                .media(mediaList)
                .build();


        // 5. 调用AI模型
        return chatClient.prompt()
                .messages(userMessage)
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .call()
                .content();
    }

}

