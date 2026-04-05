package com.english.langchain4j.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.english.langchain4j.domain.po.ChatType;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ChatTypeMapper extends BaseMapper<ChatType> {

    // 根据type表与record表关联，获取未过期对话列表
    @Select("SELECT * FROM english_chat_type " +
            "WHERE user_id = #{userId} " +
            "AND chat_id IN " +
            "(SELECT chat_id FROM english_chat_record WHERE user_id = #{userId} AND expired_time > NOW())")
    List<ChatType> getVaildChatList(Long userId);

}
