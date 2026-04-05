package com.english.langchain4j.domain.po;

import lombok.Data;

@Data
public class UserContent {
    private String text; // 用户发送的文本
    private String type; // 内容类型（固定为TEXT）
}
