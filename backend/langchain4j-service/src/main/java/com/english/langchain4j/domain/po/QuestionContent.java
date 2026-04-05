package com.english.langchain4j.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 试题表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("english_question_content")
public class QuestionContent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 试题ID
     */
    @TableId(value = "question_id", type = IdType.AUTO)
    private Long questionId;

    /**
     * 关联试题类型ID
     */
    private Long questionTypeId;

    /**
     * 难度（一级 ~ 九级）
     */
    private String difficulty;

    /**
     * 分值
     */
    private Integer score;

    /**
     * 要求时间（分钟）
     */
    private Integer requiredTime;

    /**
     * 题目标题
     */
    private String questionTitle;

    /**
     * 题目作者
     */
    private String questionAuthor;

    /**
     * 题目内容
     */
    private String questionText;

    /**
     * 选择题选项
     */
    private String options;

    /**
     * 听力音频URL
     */
    private String audioUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}