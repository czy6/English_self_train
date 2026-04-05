package com.english.langchain4j.utils;

public class RedisConstants {

    // 1.验证码
    public static final String LOGIN_CODE_KEY = "english:login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;

    public static final Long CACHE_NULL_TTL = 2L;

    // 2.标识当前对话是否为第一次
    public static final String CHAT_FIRST_KEY = "english:chat:first:";

    // 3.标识试题类型与类型id
    public static final String QUESTION_MAIN_TYPE_KEY = "english:question:mainType";

    // 4.标识子类型与类型id
    public static final String QUESTION_SUB_TYPE_KEY = "english:question:subType";

    // 5.每道题的最大熟悉度
    public static final String QUESTION_MAX_PROFICIENCY_KEY = "english:question:max:proficiency:";

    // 6.每个类型下的用户所做题目集合以及平均熟练度
    public static final String QUESTION_IDS_PROFICIENCY_KEY = "english:question:ids:proficiency:";

    // 7.ai聊天记录
    public static final String CHAT_RECORD_KEY = "english:chat:record:";
}
