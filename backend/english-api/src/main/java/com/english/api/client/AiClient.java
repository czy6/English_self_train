package com.english.api.client;

import com.english.api.dto.ChatImageRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("ai-service")
public interface AiClient {

    @RequestMapping(value = "/ai/chat")
    String chat(@RequestParam("prompt") String prompt, @RequestParam("chatId") String chatId);

    @RequestMapping(value = "/ai/chat/common")
    String commonChat(@RequestParam("prompt") String prompt);

    @PostMapping("/ai/chat/image")
    String chatWithImage(@RequestBody ChatImageRequestDTO request);

}
