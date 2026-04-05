package com.english.langchain4j.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.english.langchain4j.domain.po.ChatRecord;
import com.english.langchain4j.domain.vo.Result;

public interface IChatRecordService extends IService<ChatRecord> {
    Result getChatRecordByChatId(String chatId);
}
