package com.english.langchain4j.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.english.api.client.AiClient;
import com.english.api.dto.ChatImageRequestDTO;
import com.english.langchain4j.aiService.IChatWithImageService;
import com.english.langchain4j.domain.po.ChatRecord;
import com.english.langchain4j.domain.po.ChatType;
import com.english.langchain4j.domain.vo.ChatListVO;
import com.english.langchain4j.domain.vo.Result;
import com.english.langchain4j.mapper.ChatTypeMapper;
import com.english.langchain4j.repository.RedisChatMemoryStore;
import com.english.langchain4j.service.IChatRecordService;
import com.english.langchain4j.service.IChatService;
import com.english.langchain4j.aiService.IConsultService;
import com.english.langchain4j.utils.RedisConstants;
import com.english.langchain4j.utils.RedisIdWorker;
import com.english.langchain4j.utils.UserHolder;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl extends ServiceImpl<ChatTypeMapper, ChatType> implements IChatService {

    private final RedisIdWorker redisIdWorker;
    private final StringRedisTemplate stringRedisTemplate;
    private final IChatRecordService chatRecordService;
    private final RedisChatMemoryStore redisChatMemoryStore;
    private final IConsultService consultService;
    private final IChatWithImageService chatWithImageService;
    private final StreamingChatModel streamingChatModel;

    // 创建线程池用于异步处理
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * 获取对话id
     *
     * @return 对话id,类型
     */
    @Override
    public Result getChatId() {
        // 1.保存至type数据库
        ChatType ct = new ChatType();
        // id生成器
        String conversationId = String.valueOf(redisIdWorker.nextId("aiChat"));
        ct.setChatId(conversationId);
        ct.setTitle("对话_" + conversationId);
        ct.setChatType("chat");
        LocalDateTime startTime = LocalDateTime.now();
        ct.setStartTime(startTime);
        ct.setUserId(UserHolder.getUserId());
        boolean isSuccess = save(ct);
        if (!isSuccess) {
            return Result.fail("创建对话失败");
        }
        // 2.标记为第一次对话，保存至redis--方便后续修改标题
        stringRedisTemplate.opsForSet().add(RedisConstants.CHAT_FIRST_KEY + UserHolder.getUserId(), conversationId);
        // 3.返回结果
        ChatListVO chatListVO = BeanUtil.copyProperties(ct, ChatListVO.class);
        return Result.ok(chatListVO);
    }

    /**
     * 获取对话列表
     * @return 对话列表
     */
    @Override
    public Result getChatList() {
        List<ChatType> chatList = baseMapper.getVaildChatList(UserHolder.getUserId());
        if(chatList != null) {
            List<ChatListVO> chatListVOList = BeanUtil.copyToList(chatList, ChatListVO.class);
            return Result.ok(chatListVOList);
        }
        return Result.ok(List.of());
    }

    /**
     * 根据会话id删除单个对话
     * @param chatId 对话id
     * @return ok
     */
    @Transactional
    @Override
    public Result deleteChat(String chatId) {
        ChatType record = lambdaQuery().eq(ChatType::getChatId, chatId).one();
        if(record == null) {
            return Result.fail("会话不存在，删除失败！");
        }
        Long userId = UserHolder.getUserId();
        // 1.先逻辑删除record表中的对话(有的话)
        List<ChatRecord> recordList = chatRecordService.lambdaQuery()
                .eq(ChatRecord::getUserId, userId)
                .eq(ChatRecord::getChatId, chatId)
                .list();
        if(recordList != null && !recordList.isEmpty()) {
            try {
                redisChatMemoryStore.deleteMessages(chatId);
            }catch (Exception e){
                return Result.fail("删除失败");
            }
        }
        // 2.删除type表中的对话(外键依赖)
        boolean isSuccess = lambdaUpdate().eq(ChatType::getUserId, userId).eq(ChatType::getChatId, chatId).remove();
        if (isSuccess) {
            stringRedisTemplate.opsForSet().remove(RedisConstants.CHAT_FIRST_KEY + userId, chatId);
        } else {
            return Result.fail("删除失败");
        }
        return Result.ok();

    }

    /**
     * 删除某个用户所有对话
     * @return ok
     */
    @Transactional
    @Override
    public Result deleteAllChat() {
        // 1.先删除record表中的未过期对话
        List<ChatRecord> recordList = chatRecordService.lambdaQuery()
                .eq(ChatRecord::getUserId, UserHolder.getUserId())
                .gt(ChatRecord::getExpiredTime, LocalDateTime.now())
                .list();
        if(recordList != null && !recordList.isEmpty()) {
            try {
                for (ChatRecord chatRecord : recordList) {
                    redisChatMemoryStore.deleteMessages(chatRecord.getChatId());
                }
            }catch (Exception e){
                return Result.fail("删除失败");
            }
        }
        boolean isSuccess = lambdaUpdate().eq(ChatType::getUserId, UserHolder.getUserId()).remove();
        if (isSuccess) {
            stringRedisTemplate.delete(RedisConstants.CHAT_FIRST_KEY + UserHolder.getUserId());
        } else {
            return Result.fail("清空失败");
        }
        return Result.ok();
    }


    /**
     * 进行对话
     * @param prompt 对话内容
     * @param chatId 对话id
     * @return 回复内容
     */
    @Transactional
    @Override
    public Result chat(String prompt, String chatId) {
        // 1.如果是第一次对话，修改type表中的对话名称
        modifyTitle(chatId, prompt);
        // 2.进行对话
        String replyMessage = consultService.chat(chatId, prompt);
        return Result.ok(replyMessage);
    }

    /**
     * 进行对话带图片
     */

    @Override
    public Result chatWithImage(ChatImageRequestDTO request) {
        String chatId = request.getChatId();
        String prompt = request.getPrompt();
        List<String> imageList = request.getImageUrls();
        // 1.如果是第一次对话，修改type表中的对话名称
        modifyTitle(chatId, prompt);
        // 2.进行对话
        String processMessage = prompt + imageList.toString();
        String replyMessage = chatWithImageService.chatWithImage(chatId, processMessage);
        return Result.ok(replyMessage);
    }


    /**
     * 流式对话实现
     * @param prompt 对话内容
     * @param chatId 对话 id
     * @param emitter SSE 发射器
     */
    @Override
    public void chatStream(String prompt, String chatId, SseEmitter emitter) {
        log.info("chatStream 方法被调用 - chatId: {}, prompt: {}", chatId, prompt);
        
        // 1.如果是第一次对话，修改 type 表中的对话名称
        modifyTitle(chatId, prompt);

        // 2.保存当前用户 ID（因为异步线程中无法获取 ThreadLocal）
        Long userId = UserHolder.getUserId();
        log.info("chatStream 获取到用户 ID - chatId: {}, userId: {}", chatId, userId);

        // 3.构建完整的回复内容（用于保存到 ChatMemory）
        StringBuilder fullResponse = new StringBuilder();

        // 4.异步处理流式响应
        CompletableFuture.runAsync(() -> {
            // 5.在异步线程中设置用户 ID
            UserHolder.setUserId(userId);
            log.info("异步线程中设置用户 ID - chatId: {}, userId: {}", chatId, userId);
            
            try {
                // 使用 IConsultService 的流式方法，并收集完整响应
                log.info("开始调用 consultService.chatStream - chatId: {}", chatId);
                
                consultService.chatStream(chatId, prompt)
                    .collectList()  // 收集所有 token 为 List
                    .subscribe(
                        // 接收完整响应
                        tokens -> {
                            try {
                                log.info("收到 AI 回复，token 数量：{} - chatId: {}", tokens.size(), chatId);
                                
                                // 拼接完整回复
                                String reply = String.join("", tokens);
                                
                                log.info("AI 完整回复：{} - chatId: {}", reply, chatId);
                                
                                // 流式发送到前端
                                for (String token : tokens) {
                                    emitter.send(SseEmitter.event()
                                            .name("message")
                                            .data(token));
                                }
                                
                                // 发送完成信号
                                emitter.send(SseEmitter.event()
                                        .name("done")
                                        .data("[DONE]"));
                                
                                // 完成连接
                                emitter.complete();
                                
                                log.info("流式对话完成 - chatId: {}, AI 回复：{}", chatId, reply);
                                
                                // 注意：LangChain4j 的 Flux 方法会自动保存到 ChatMemory
                                // 不需要手动保存
                                
                            } catch (IOException e) {
                                log.error("发送 SSE 消息失败", e);
                                emitter.completeWithError(e);
                            }
                        },
                        // 错误处理
                        error -> {
                            log.error("Flux 订阅错误 - chatId: {}", chatId, error);
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("error")
                                        .data("服务异常：" + error.getMessage()));
                                emitter.completeWithError(error);
                            } catch (IOException e) {
                                log.error("发送错误消息失败", e);
                            }
                        }
                    );

            } catch (Exception e) {
                log.error("流式对话异常", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("服务异常：" + e.getMessage()));
                    emitter.completeWithError(e);
                } catch (IOException ex) {
                    log.error("发送错误消息失败", ex);
                }
            } finally {
                // 6.清理用户 ID，防止内存泄漏
                log.info("清理异步线程用户 ID - chatId: {}", chatId);
                UserHolder.removeUserId();
            }
        }, executor);
        
        log.info("chatStream 方法返回 - chatId: {}", chatId);
    }

    /**
     * 修改对话标题
     * @param chatId 对话 id
     * @param prompt 对话内容
     */
    public void modifyTitle(String chatId, String prompt) {
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(RedisConstants.CHAT_FIRST_KEY + UserHolder.getUserId(), chatId);
        if (Boolean.TRUE.equals(isMember)) {
            lambdaUpdate().eq(ChatType::getChatId, chatId).set(ChatType::getTitle, prompt).update();
            stringRedisTemplate.opsForSet().remove(RedisConstants.CHAT_FIRST_KEY+ UserHolder.getUserId(), chatId);
        }
    }

}
