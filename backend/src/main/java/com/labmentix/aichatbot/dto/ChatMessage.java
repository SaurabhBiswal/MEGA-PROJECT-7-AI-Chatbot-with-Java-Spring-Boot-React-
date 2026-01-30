package com.labmentix.aichatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    private String content;
    private String sender;
    private Long conversationId;
    private MessageType type;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        CONVERSATION_UPDATE
    }
}
