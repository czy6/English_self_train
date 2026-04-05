package com.english.langchain4j.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.english.api.dto.ChatImageRequestDTO;
import com.english.langchain4j.domain.po.ChatType;
import com.english.langchain4j.domain.vo.Result;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IChatService extends IService<ChatType> {
    Result getChatId();

    Result getChatList();

    Result deleteChat(String chatId);

    Result deleteAllChat();

    Result chat(String prompt, String chatId);

    Result chatWithImage(ChatImageRequestDTO request);

    /**
     * 流式对话
     * @param prompt 对话内容
     * @param chatId 对话 id
     * @param emitter SSE 发射器
     */
    void chatStream(String prompt, String chatId, SseEmitter emitter);
}
