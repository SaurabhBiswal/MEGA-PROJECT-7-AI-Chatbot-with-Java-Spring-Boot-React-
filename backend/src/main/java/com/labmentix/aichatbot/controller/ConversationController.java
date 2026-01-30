package com.labmentix.aichatbot.controller;

import com.labmentix.aichatbot.dto.RenameRequest;
import com.labmentix.aichatbot.model.Conversation;
import com.labmentix.aichatbot.model.Message;
import com.labmentix.aichatbot.model.User;
import com.labmentix.aichatbot.repository.ConversationRepository;
import com.labmentix.aichatbot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@CrossOrigin(origins = "http://localhost:5173")
public class ConversationController {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Conversation>> getConversations(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(conversationRepository.findByUserOrderByStartedAtDesc(user));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        return ResponseEntity.ok(conversation.getMessages());
    }

    @PostMapping
    public ResponseEntity<Conversation> createConversation(Authentication authentication, @RequestBody String title) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Conversation conversation = Conversation.builder()
                .user(user)
                .title(title.replace("\"", "")) // Simple cleanup
                .build();
        return ResponseEntity.ok(conversationRepository.save(conversation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Conversation> updateConversation(
            Authentication authentication,
            @PathVariable("id") Long id,
            @RequestBody RenameRequest request) {

        System.out.println("DEBUG: Renaming conversation " + id + " to " + request.getTitle());

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Security check: ensure user owns the conversation
        if (!conversation.getUser().getUsername().equals(authentication.getName())) {
            return ResponseEntity.status(403).build();
        }

        conversation.setTitle(request.getTitle().replace("\"", ""));
        return ResponseEntity.ok(conversationRepository.save(conversation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(
            Authentication authentication,
            @PathVariable("id") Long id) {

        System.out.println("DEBUG: Deleting conversation " + id);

        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Security check: ensure user owns the conversation
        if (!conversation.getUser().getUsername().equals(authentication.getName())) {
            return ResponseEntity.status(403).build();
        }

        conversationRepository.delete(conversation);
        return ResponseEntity.ok().build();
    }
}
