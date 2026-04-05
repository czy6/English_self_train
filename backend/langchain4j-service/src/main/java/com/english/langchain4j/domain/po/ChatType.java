package com.english.langchain4j.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 存储用户对话的类型和时间信息
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("english_chat_type")
public class ChatType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 对话id
     */
    @TableId(value = "chat_id", type = IdType.INPUT)
    private String chatId;

    /**
     * 用户ID（关联到用户表）
     */
    @TableField(value = "user_id", insertStrategy = FieldStrategy.NOT_NULL)
    private Long userId;

    /**
     * 对话类型
     */
    @TableField(value = "chat_type")
    private String chatType;

    /**
     * 对话名称
     */
    @TableField(value = "title")
    private String title;

    /**
     * 对话开始时间
     */
    @TableField(value = "start_time")
    private LocalDateTime startTime;

    /**
     * 对话结束时间（可为空）
     */
    @TableField(value = "end_time")
    private LocalDateTime endTime;


}