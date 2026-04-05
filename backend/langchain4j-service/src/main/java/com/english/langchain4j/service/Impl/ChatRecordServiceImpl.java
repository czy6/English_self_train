package com.english.langchain4j.service.Impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.english.langchain4j.domain.po.ChatRecordMessage;
import com.english.langchain4j.domain.po.ChatRecord;
import com.english.langchain4j.domain.vo.ChatRecordVO;
import com.english.langchain4j.domain.vo.Result;
import com.english.langchain4j.mapper.ChatRecordMapper;
import com.english.langchain4j.repository.RedisChatMemoryStore;
import com.english.langchain4j.service.IChatRecordService;
import com.english.langchain4j.utils.UserHolder;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.english.langchain4j.utils.RedisConstants.CHAT_RECORD_KEY;

@Service
@RequiredArgsConstructor
public class ChatRecordServiceImpl extends ServiceImpl<ChatRecordMapper, ChatRecord> implements IChatRecordService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 获取指定聊天记录
     *
     * @param chatId 聊天id
     * @return 聊天记录
     */
    @Override
    public Result getChatRecordByChatId(String chatId) {
        // 1.查询未过期(逻辑删除)的聊天记录,redis -> mysql
        List<ChatRecordMessage> chatRecordMessages = getAllChatRecord(chatId);
        if (chatRecordMessages.isEmpty()) {
            return Result.ok(List.of());
        }

        // 2.解析聊天记录 -> List<ChatRecordVO>
        // 2.1.筛选出 USER 和 AI 的对话记录（忽略 SYSTEM 类型）
        List<ChatRecordVO> chatRecordVOList = new ArrayList<>();
        // 2.2.解析出对应内容，并添加到 VO 中返回给用户
        for (ChatRecordMessage message : chatRecordMessages) {
            String type = message.getType();
            ChatRecordVO chatRecordVO = new ChatRecordVO();
            if ("USER".equals(type)) {
                // 处理用户消息
                String userText = "";
                if (message.getContents() != null && !message.getContents().isEmpty()) {
                    userText = message.getContents().get(0).getText();
                }
                chatRecordVO.setRole("user");
                chatRecordVO.setContent(userText);
            } else if ("AI".equals(type)) {
                // AI 的消息内容在 text 中
                chatRecordVO.setRole("assistant");
                chatRecordVO.setContent(message.getText() != null ? message.getText() : "");
            } else {
                continue;
            }
            chatRecordVOList.add(chatRecordVO);
        }
        return Result.ok(chatRecordVOList);
    }

    /**
     * 获取所有聊天记录
     *
     * @param chatId 聊天 id
     * @return 所有聊天记录
     */
    private List<ChatRecordMessage> getAllChatRecord(String chatId) {
        // 0.key
        String key = CHAT_RECORD_KEY + UserHolder.getUserId() + ":" + chatId;

        // 1. 先查 Redis
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json != null) {
            return JSONUtil.toList(json, ChatRecordMessage.class);
        }

        // 2. 再查 MySQL
        ChatRecord chatRecord = lambdaQuery()
                .eq(ChatRecord::getChatId, chatId)
                .gt(ChatRecord::getExpiredTime, LocalDateTime.now())
                .one();
        if (chatRecord != null && chatRecord.getChatRecord() != null) {
            // 回写 Redis（缓存预热）
            stringRedisTemplate.opsForValue().set(
                    key,
                    chatRecord.getChatRecord(),
                    Duration.ofDays(1)
            );
            return JSONUtil.toList(chatRecord.getChatRecord(), ChatRecordMessage.class);
        }

        return List.of();
    }

}
