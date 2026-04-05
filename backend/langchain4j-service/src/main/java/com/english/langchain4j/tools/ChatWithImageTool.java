package com.english.langchain4j.tools;

import com.english.api.client.AiClient;
import com.english.api.dto.ChatImageRequestDTO;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWithImageTool {


    private final AiClient aiClient;

    @Tool("对话中包含图片url地址列表")
    public String chatWithImage(
            @P("对话id") String chatId,
            @P("图片url地址列表") List<String> imageUrls,
            @P("文本描述") String prompt
    ) {
        System.out.println("chatId = " + chatId);
        System.out.println("imageUrls = " + imageUrls);
        System.out.println("prompt = " + prompt);
        ChatImageRequestDTO request = new ChatImageRequestDTO();
        request.setChatId(chatId);
        request.setPrompt(prompt);
        request.setImageUrls(imageUrls);
        String replyMessage = aiClient.chatWithImage(request);
        String suggest = "\n同时，我想说明一下，我是一个英语学习智能小助手，主要功能是辅助您进行英语学习和训练，并不是用于平常对话的工具。" +
                            "如果在某些图片或文件的处理上无法给出有效回答，还请多多谅解！😊";
        return replyMessage + suggest;
    }

}
