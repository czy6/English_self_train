package com.english.langchain4j.domain.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("english_chat_record")
public class ChatRecord {

    /**
     * 会话ID
     */
    @TableId(value = "chat_id", type = IdType.INPUT)
    private String chatId;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 对话列表（JSON）
     */
    @TableField(value = "chat_record", insertStrategy = FieldStrategy.NOT_NULL)
    private String chatRecord;

    /**
     * 图片链接
     */
    @TableField(value = "image_urls")
    private String imageUrls;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    private LocalDateTime createdTime;

    /**
     * 修改时间
     */
    @TableField(value = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 过期时间(逻辑过期)
     */
    @TableField(value = "expired_time")
    private LocalDateTime expiredTime;

}
