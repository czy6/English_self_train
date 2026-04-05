package com.english.langchain4j.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatListVO {
    String chatId;
    String chatType;
    String title;
    LocalDateTime startTime;
}
