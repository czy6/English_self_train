package com.english.langchain4j.repository;

import com.english.langchain4j.domain.po.ChatRecord;
import com.english.langchain4j.service.IChatRecordService;
import com.english.langchain4j.utils.UserHolder;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static com.english.langchain4j.utils.RedisConstants.CHAT_RECORD_KEY;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final StringRedisTemplate stringRedisTemplate;
    private final IChatRecordService chatRecordService;

    @Override
    public List<ChatMessage> getMessages(Object chatId) {
        // 0.key
        String key = CHAT_RECORD_KEY + UserHolder.getUserId() + ":" + chatId.toString();

        // 1. 先查 Redis
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json != null) {
            return ChatMessageDeserializer.messagesFromJson(json);
        }

        // 2. 再查 MySQL
        ChatRecord chatRecord = chatRecordService.lambdaQuery()
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
            return ChatMessageDeserializer.messagesFromJson(chatRecord.getChatRecord());
        }

        return List.of();
    }

    @Override
    public void updateMessages(Object chatId, List<ChatMessage> list) {
        // 更新会话消息
        Long userId = UserHolder.getUserId();
        log.info("updateMessages 被调用 - chatId: {}, userId: {}, messageCount: {}", 
                chatId, userId, list.size());
        
        // 1.list->json
        String json = ChatMessageSerializer.messagesToJson(list);

        // 2.写入 MySQL
        ChatRecord chatRecord = new ChatRecord();
        chatRecord.setChatId(chatId.toString());
        chatRecord.setUserId(userId);
        chatRecord.setChatRecord(json);
        chatRecord.setCreatedTime(LocalDateTime.now());
        chatRecord.setUpdatedTime(LocalDateTime.now());
        chatRecord.setExpiredTime(LocalDateTime.now().plusDays(30));  // 30 天后过期
        boolean isSuccess = chatRecordService.saveOrUpdate(chatRecord);
        
        log.info("updateMessages 保存结果 - chatId: {}, isSuccess: {}", chatId, isSuccess);

        // 2.json->redis
        if (isSuccess) {
            stringRedisTemplate.opsForValue().set(
                    CHAT_RECORD_KEY + userId + ":" + chatId,
                    json,
                    Duration.ofDays(1)
            );
        }
    }

    @Override
    public void deleteMessages(Object chatId) {
        // 1.删除 Redis 中的记录
        Boolean isSuccess = stringRedisTemplate.delete(
                CHAT_RECORD_KEY + UserHolder.getUserId() + ":" + chatId.toString()
        );

        // 2.删除 MySQL 中的记录(逻辑删除)
        // (expire_time IS NULL OR expire_time > NOW())  -- 只查未过期的记录
        if (Boolean.TRUE.equals(isSuccess)) {
            ChatRecord chatRecord = new ChatRecord();
            chatRecord.setChatId(chatId.toString());
            chatRecord.setExpiredTime(LocalDateTime.now());  // 标记为已过期
            chatRecord.setUpdatedTime(LocalDateTime.now());  // 更新时间戳
            chatRecordService.updateById(chatRecord);  // 只更新 expire_time 和 updated_time
        }
    }
}
