package com.english.ai.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

@Data
@NoArgsConstructor
public class MessageVO {

    private String role;
    private String content;


    public MessageVO(Message message) {
        switch (message.getMessageType()) {
            case USER -> this.role = "user";
            case ASSISTANT -> this.role = "assistant";
            default -> this.role = "";
        }
        this.content = message.getText();
    }

}
