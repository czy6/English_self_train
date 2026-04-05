package com.english.langchain4j;

import com.english.api.client.AiClient;
import com.english.api.client.QuestionClient;
import com.english.langchain4j.aiService.IRecommendService;
import com.english.langchain4j.service.IChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.alibaba.dashscope.app.*;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class Langchain4JApplicationTests {


    @Autowired
    private QuestionClient questionClient;

    @Autowired
    private AiClient aiClient;

    @Autowired
    private IRecommendService recommendService;

    @Autowired
    private IChatService chatService;

    static void appCall()
            throws ApiException, NoApiKeyException, InputRequiredException {
        ApplicationParam param = ApplicationParam.builder()
                // 若没有配置环境变量，可用百炼API Key将下行替换为：.apiKey("sk-xxx")。但不建议在生产环境中直接将API Key硬编码到代码中，以减少API Key泄露风险。
                .apiKey("sk-3c8e8b94d93f48aba44eddacbbb6084f")
                .appId("3237726b736d438785a3f4418c9574b5")
                .prompt("你是谁？")
                .build();

        Application application = new Application();
        ApplicationResult result = application.call(param);

        System.out.printf("text: %s\n",
                result.getOutput().getText());
    }

    @Test
    void testRecommendService() {
//        String reply = questionClient.recommendQuestion(2L, "阅读", "应用文");
//        System.out.println("reply = " + reply);
//        String s = aiClient.commonChat("你好啊");
//        System.out.println("s = " + s);
        String recommend = recommendService.recommend("试题主类型：阅读，试题子类型：说明文，试题难度：四级");
        System.out.println("recommend = " + recommend);
    }

    @Test
    void testAgent(){
        try {
            appCall();
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            System.err.println("message："+e.getMessage());
            System.out.println("请参考文档：https://help.aliyun.com/zh/model-studio/developer-reference/error-code");
        }
        System.exit(0);
    }

}
