package com.english.langchain4j.domain.po;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

// 单条聊天消息（对应数组中的每个元素）
@Data
public class ChatRecordMessage {
    // 消息类型：SYSTEM/USER/AI
    private String type;

    // USER类型的消息内容（包含text和type）
    private List<UserContent> contents;

    // AI类型的消息文本
    private String text;

    // 工具调用请求（AI可能包含，不影响对话解析，可忽略）
    @JsonProperty("toolExecutionRequests")
    private List<Object> toolExecutionRequests;
}