package com.english.langchain4j.service.Impl;

import com.english.api.client.QuestionClient;
import com.english.api.dto.SaveQuestionDTO;
import com.english.langchain4j.aiService.ICetExamTestService;
import com.english.langchain4j.aiService.IRecommendService;
import com.english.langchain4j.aiService.ISuggestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendImpl {

    private final ISuggestService suggestService;
    private final IRecommendService recommendService;
    private final ICetExamTestService cetExamTestService;
    private final RabbitTemplate rabbitTemplate;

    /**
     * ai创建试题
     *
     * @param recommendContent "试题主类型：xxx，试题子类型：xxx，试题难度：xxx"
     */
    public String createQuestionByAI(String recommendContent) {
        // 1.获取ai生成的json格式试题
        String subType = "";
        String difficulty = "";
        String aiJson = "";
        try {
            aiJson = recommendService.recommend(recommendContent);
        } catch (Exception e) {
            log.info("生成试题过程中出现错误: " + e.getMessage());
        }

        // 2.数据库入库操作 -> mq
        String[] parts = recommendContent.split("，");
        for (String part : parts) {
            if (part.contains("试题子类型")) {
                subType = part.split("：")[1];
            } else if (part.contains("试题难度")) {
                difficulty = part.split("：")[1];
            }
        }
        SaveQuestionDTO saveQuestionDTO = new SaveQuestionDTO();
        saveQuestionDTO.setSubType(subType);
        saveQuestionDTO.setDifficulty(difficulty);
        saveQuestionDTO.setAiJson(aiJson);
        String questionId = null;
        try {
            Object obj = rabbitTemplate.convertSendAndReceive("question.direct", "update.question", saveQuestionDTO);
            if (obj != null) {
                questionId = obj.toString();
            } else {
                return "推荐试题信息失败！请联系管理员";
            }
        }catch (Exception e) {
            log.error("更新试题信息失败：" + e);
        }


        // 3.拼接试题信息
        String questionMessage = recommendContent + "，试题编号：" + questionId + "\n" + aiJson;

        return createSuggestion(questionMessage);
    }

    /**
     * 生成试题推荐建议
     * @param questionMessage 试题信息
     * @return 试题推荐建议
     */
    public String createSuggestion(String questionMessage) {
        return suggestService.suggest(questionMessage);
    }

    // 在RecommendImpl.java中添加以下方法
    /**
     * 处理真题入库并生成建议
     * @param mainType 试题主类型
     * @param examId 真题标识（如2018.12(I)）
     * @param passageNumber 篇目序号
     * @return 试题推荐建议
     */
    public String createRealExamQuestion(String mainType, String examId, String passageNumber) {
        // 1. 根据信息查找RAG库
        String cetMessage = "试题主类型："+ mainType +"，真题标识："+examId+"，篇目序号："+passageNumber;

        // 2. 解析出json
        String json = cetExamTestService.cetExamTest(cetMessage);
        System.out.println("json = " + json);

        // 2. 发送MQ入库
//        String questionId = null;
//        try {
//            Object obj = rabbitTemplate.convertSendAndReceive("question.direct", "update.question", saveQuestionDTO);
//            if (obj != null) {
//                questionId = obj.toString();
//            } else {
//                return "真题入库失败！请联系管理员";
//            }
//        } catch (Exception e) {
//            log.error("真题入库失败：" + e);
//            return "真题入库失败！";
//        }
//
//        // 3. 拼接真题信息并生成建议
//        String questionMessage = String.format(
//                "试题主类型：%s，试题子类型：%s，真题标识：%s，篇目序号：%d，试题编号：%s",
//                mainType, subType, examId, passageNumber, questionId
//        );
//        return createSuggestion(questionMessage);

        return "四六级真题系统维护中，请稍后再试....";
    }
}
