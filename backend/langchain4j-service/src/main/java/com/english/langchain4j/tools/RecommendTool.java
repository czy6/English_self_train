package com.english.langchain4j.tools;

import com.english.api.client.QuestionClient;
import com.english.api.dto.SaveQuestionDTO;
import com.english.langchain4j.service.Impl.RecommendImpl;
import com.english.langchain4j.utils.UserHolder;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendTool {

    private final QuestionClient questionClient;
    private final RecommendImpl recommendImpl;

    @Tool("推荐试题服务")
    public String recommendQuestion(
            @P("试题主类型：听力，阅读，翻译，写作") String mainType,
            @P("试题子类型") String subType,
            @P("真题标识，如2018.12(I)") String examId,
            @P("篇目序号，如1") String passageNumber
    ){
        System.out.println("正在推荐试题...");
        System.out.println("主类型: " + mainType + "，子类型: " + subType + "，真题标识: " + examId + "，篇目序号: " + passageNumber);
        try {
            // 1.个性化推荐：
            // 1.1.数据库存在: "DataBase -- " + "主类型，子类型，难度，熟练度，试题编号" + questionContent.toString()
            // 1.2.待ai生成: "AI -- " + "主类型，子类型，难度，熟练度"
            // 2.四六级真题：
            // 2.1.数据库存在: "CET-DataBase -- 试题编号：xxx"
            // 2.2.待ai查询RAG: "CET-AI"
            String recommendContent = questionClient.recommendQuestion(UserHolder.getUserId(), mainType, subType, examId, passageNumber);
            System.out.println("recommendContent = " + recommendContent);
            if (examId != null && !examId.trim().isEmpty()) {
                // 3.四六级真题
                if (recommendContent.startsWith("CET-DataBase")) {
                    // 3.1.数据库存在 --> 返回用户试题
                    String questionId = recommendContent.substring(recommendContent.indexOf("：") + 1);
                    return "已为你找到 [" + examId + "-" + mainType + passageNumber + "] 对应的试题，接下来就是沉浸式训练的时刻了！" +
                                   "\n准备好了吗？现在就启动训练，让每一道题都成为你积累实力的基石，一步步夯实基础、突破瓶颈，为四六级考试积攒充足底气！" +
                                   "\n<!-- JUMP:" + mainType + "-" + questionId + "-->";
                } else if (recommendContent.startsWith("CET-AI")) {
                    // 3.2.待ai查询RAG --> 生成对应json格式试题 --> 入库
                    String realExamResult = recommendImpl.createRealExamQuestion(mainType, examId, passageNumber);
                    System.out.println("真题建议内容 = " + realExamResult);
                    return realExamResult;
                }
            }else if (recommendContent.startsWith("DataBase")) {
                // 4.个性化推荐 数据库存在 ---> 直接生成建议
                String suggestion = recommendImpl.createSuggestion(recommendContent);
                System.out.println("建议内容 = " + suggestion);
                return "一次不差返回给用户：" + suggestion;
            } else if (recommendContent.startsWith("AI")) {
                // 5.个性化推荐 调用ai生成对应json格式试题 --> 入库 --> 生成建议
                String questionByAI = recommendImpl.createQuestionByAI(recommendContent);
                System.out.println("建议内容 = " + questionByAI);
                return "一次不差返回给用户：" + questionByAI;
            }
        } catch (Exception e) {
            log.error("推荐过程中出现错误: " + e.getMessage());
            return "推荐失败！";
        }
        return "推荐失败！请联系管理员";
    }


}
