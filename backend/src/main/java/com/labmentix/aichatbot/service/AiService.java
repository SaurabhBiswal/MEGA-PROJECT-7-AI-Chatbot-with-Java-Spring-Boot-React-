package com.labmentix.aichatbot.service;

import java.util.concurrent.CompletableFuture;

public interface AiService {
    CompletableFuture<String> generateResponse(String userMessage);

    CompletableFuture<String> generateResponse(String userMessage, String attachmentUrl, String attachmentType);

    CompletableFuture<String> generateResponse(String userMessage, String attachmentUrl, String attachmentType,
            String extractedText);

    CompletableFuture<String> generateTitle(String firstMessage);
}
