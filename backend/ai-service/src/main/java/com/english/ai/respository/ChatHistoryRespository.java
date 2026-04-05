package com.english.ai.respository;

import java.util.List;

public interface ChatHistoryRespository {

    // 保存会话记录
    void save(String type, String chatId); 

    // 获取会话记录
    List<String> getChatIds(String type);

}
