package com.english.langchain4j.controller;

import com.english.api.dto.ChatImageRequestDTO;
import com.english.langchain4j.aiService.ISuggestService;
import com.english.langchain4j.domain.vo.Result;
import com.english.langchain4j.service.IChatRecordService;
import com.english.langchain4j.aiService.IConsultService;
import com.english.langchain4j.service.IChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ChatController {

    private final ISuggestService suggestService;
    private final IChatService chatService;
    private final IChatRecordService chatRecordService;

    /**
     * 获取对话 id 接口
     *
     * @return 对话 id
     */
    @GetMapping("/chat/id")
    public Result getChatId() {
        return chatService.getChatId();
    }

    /**
     * 获取对话列表接口
     *
     * @return 对话列表
     */
    @GetMapping("/chat/list")
    public Result getChatList() {
        return chatService.getChatList();
    }

    /**
     * 根据对话 id 删除对话记录接口
     *
     * @param chatId 对话 id
     */
    @DeleteMapping("/chat/delete/{chatId}")
    public Result deleteChat(@PathVariable("chatId") String chatId) {
        return chatService.deleteChat(chatId);
    }

    /**
     * 删除所有对话记录接口
     */
    @DeleteMapping("/chat/delete/all")
    public Result deleteAllChat() {
        return chatService.deleteAllChat();
    }

    /**
     * 获取历史对话列表接口
     * @param chatId 对话 id
     * @return 历史对话列表
     */
    @GetMapping("/chat/history")
    public Result getChatHistory(@RequestParam("chatId") String chatId) {
        return chatRecordService.getChatRecordByChatId(chatId);
    }

    /**
     * 进行对话接口--仅文本对话（普通版本，非流式）
     *
     * @param prompt   输入内容
     * @param chatId   对话 id
     * @return 对话结果
     */
    @RequestMapping("/chat/text")
    public Result chat(@RequestParam("prompt") String prompt, @RequestParam("chatId") String chatId) {
        log.info("普通对话请求 - chatId: {}, prompt: {}", chatId, prompt);
        Result result = chatService.chat(prompt, chatId);
        log.info("普通对话响应 - chatId: {}, result: {}", chatId, result);
        return result;
    }

    /**
     * 进行对话接口--SSE 流式输出
     * 支持前端接收流式数据，实现打字机效果
     *
     * @param prompt   输入内容
     * @param chatId   对话 id
     * @return SSE 流式响应
     */
    @RequestMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestParam("prompt") String prompt,
            @RequestParam("chatId") String chatId) {
        
        log.info("开始 SSE 流式对话 - chatId: {}, prompt: {}", chatId, prompt);
        
        // 创建 SSE 发射器（5 分钟超时）
        SseEmitter emitter = new SseEmitter(300000L);
        
        // 设置完成回调
        emitter.onCompletion(() -> {
            log.info("SSE 连接完成 - chatId: {}", chatId);
        });
        
        // 设置超时回调
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时 - chatId: {}", chatId);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("连接超时"));
            } catch (IOException e) {
                log.error("发送超时消息失败", e);
            }
        });
        
        // 设置错误回调
        emitter.onError(throwable -> {
            log.error("SSE 连接错误 - chatId: {}", chatId, throwable);
        });
        
        // 异步处理流式响应
        try {
            chatService.chatStream(prompt, chatId, emitter);
        } catch (Exception e) {
            log.error("流式对话失败", e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("服务异常：" + e.getMessage()));
                emitter.completeWithError(e);
            } catch (IOException ex) {
                log.error("发送错误消息失败", ex);
            }
        }
        
        return emitter;
    }

    /**
     * 进行对话接口--分配给 question 服务的 suggest 接口
     *
     * @param prompt   输入内容
     * @return 对话结果
     */
    @RequestMapping("/suggest")
    public String suggest(@RequestParam("prompt") String prompt) {
        return suggestService.suggest(prompt);
    }

    /**
     * 进行对话接口--文字 + 图片对话
     *
     * @param request   输入内容
     * @return 对话结果
     */
    @PostMapping(value = "chat/image", consumes = "application/json")
    public Result chatWithImage(@RequestBody ChatImageRequestDTO request){
        return chatService.chatWithImage(request);
    }


}
