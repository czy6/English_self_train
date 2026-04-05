package com.english.api.client;

import com.english.api.dto.SaveQuestionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("question-service")
public interface QuestionClient {

    @PostMapping("/question/recommend")
    String recommendQuestion(
            @RequestParam("userId") Long userId,
            @RequestParam("mainType") String mainType,
            @RequestParam("subType") String subType,
            @RequestParam("examId") String examId,
            @RequestParam("passageNumber") String passageNumber
    );

}
