package com.english.ai.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatImageRequestDTO {
    private String prompt;       // 用户文本提示
    private String chatId;       // 会话ID
    private List<String> imageUrls; // 图片url列表
}