package com.labmentix.aichatbot.repository;

import com.labmentix.aichatbot.model.Conversation;
import com.labmentix.aichatbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUserOrderByStartedAtDesc(User user);
}
