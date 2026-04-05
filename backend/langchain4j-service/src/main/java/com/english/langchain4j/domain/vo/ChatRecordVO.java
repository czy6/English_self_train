package com.english.langchain4j.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatRecordVO {
    private String role;
    private String content;
}
