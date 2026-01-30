package com.labmentix.aichatbot.service;

import com.labmentix.aichatbot.dto.ChatMessage;
import com.labmentix.aichatbot.model.Conversation;
import com.labmentix.aichatbot.model.Message;
import com.labmentix.aichatbot.model.MessageType;
import com.labmentix.aichatbot.model.User;
import com.labmentix.aichatbot.repository.ConversationRepository;
import com.labmentix.aichatbot.repository.MessageRepository;
import com.labmentix.aichatbot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ChatService {

        @Autowired
        private MessageRepository messageRepository;

        @Autowired
        private ConversationRepository conversationRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private AiService aiService;

        @Autowired
        private SimpMessagingTemplate messagingTemplate;

        @Transactional
        public void processMessage(ChatMessage chatMessage) {
                // 1. Save User Message
                User sender = userRepository.findByUsername(chatMessage.getSender())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Conversation conversation;
                if (chatMessage.getConversationId() != null) {
                        conversation = conversationRepository.findById(chatMessage.getConversationId())
                                        .orElseThrow(() -> new RuntimeException("Conversation not found"));
                } else {
                        // Fallback or create new
                        conversation = conversationRepository.findByUserOrderByStartedAtDesc(sender).stream()
                                        .findFirst()
                                        .orElseGet(() -> conversationRepository.save(Conversation.builder()
                                                        .user(sender)
                                                        .startedAt(LocalDateTime.now())
                                                        .title("New Chat")
                                                        .build()));
                }

                Message userMsg = Message.builder()
                                .content(chatMessage.getContent())
                                .sender(sender)
                                .conversation(conversation)
                                .timestamp(LocalDateTime.now())
                                .type(MessageType.USER)
                                .build();

                messageRepository.save(userMsg);

                // Update conversation title intelligently if it's "New Chat" or Untitled
                if (conversation.getTitle() == null ||
                                conversation.getTitle().trim().equalsIgnoreCase("New Chat") ||
                                conversation.getTitle().trim().isEmpty()) {

                        aiService.generateTitle(chatMessage.getContent()).thenAccept(aiTitle -> {
                                conversation.setTitle(aiTitle);
                                conversationRepository.save(conversation);

                                // Notify frontend to refresh conversation list
                                ChatMessage updateMsg = ChatMessage.builder()
                                                .sender("SYSTEM")
                                                .type(ChatMessage.MessageType.CONVERSATION_UPDATE)
                                                .content("Title Updated")
                                                .conversationId(conversation.getId())
                                                .build();
                                messagingTemplate.convertAndSend("/topic/public", updateMsg);
                        });
                }

                // 2. Trigger AI Response
                aiService.generateResponse(chatMessage.getContent())
                                .thenAccept(responseContent -> {
                                        // 3. Save AI Message
                                        Message aiMsg = Message.builder()
                                                        .content(responseContent)
                                                        .sender(null) // System/AI
                                                        .conversation(conversation)
                                                        .timestamp(LocalDateTime.now())
                                                        .type(MessageType.AI)
                                                        .build();

                                        messageRepository.save(aiMsg);

                                        // 4. Broadcast AI Message
                                        ChatMessage responseDto = ChatMessage.builder()
                                                        .content(responseContent)
                                                        .sender("AI Assistant")
                                                        .type(ChatMessage.MessageType.CHAT)
                                                        .build();

                                        messagingTemplate.convertAndSend("/topic/public", responseDto);
                                });
        }
}
