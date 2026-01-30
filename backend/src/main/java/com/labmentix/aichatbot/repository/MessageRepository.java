package com.labmentix.aichatbot.repository;

import com.labmentix.aichatbot.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationId(Long conversationId);
}
