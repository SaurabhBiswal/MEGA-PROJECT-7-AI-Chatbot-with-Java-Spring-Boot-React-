package com.labmentix.aichatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labmentix.aichatbot.dto.OpenAIRequest;
import com.labmentix.aichatbot.dto.OpenAIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class AiServiceImpl implements AiService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AiServiceImpl() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Async
    public CompletableFuture<String> generateResponse(String userMessage) {
        try {
            log.info("Sending request to Groq: {}", userMessage);

            // Create Groq Request (OpenAI Compatible)
            // Using llama-3.3-70b-versatile for high performance
            OpenAIRequest.Message message = new OpenAIRequest.Message("user", userMessage);
            OpenAIRequest groqRequest = new OpenAIRequest(
                    "llama-3.3-70b-versatile",
                    List.of(message),
                    0.7,
                    4096);

            String requestBody = objectMapper.writeValueAsString(groqRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        log.info("Groq Response Status: {}", response.statusCode());

                        if (response.statusCode() != 200) {
                            log.error("Groq Error: {}", response.body());
                            return "Error from Groq: " + response.body();
                        }

                        try {
                            OpenAIResponse responseDto = objectMapper.readValue(response.body(), OpenAIResponse.class);

                            if (responseDto.getChoices() != null && !responseDto.getChoices().isEmpty()) {
                                OpenAIResponse.Choice choice = responseDto.getChoices().get(0);
                                if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                                    return choice.getMessage().getContent();
                                }
                            }

                            return "No response from AI.";
                        } catch (Exception e) {
                            log.error("Error parsing response", e);
                            return "Error parsing AI response: " + e.getMessage();
                        }
                    });
        } catch (Exception e) {
            log.error("Error generating response", e);
            return CompletableFuture.completedFuture("Error: " + e.getMessage());
        }
    }

    @Override
    @Async
    public CompletableFuture<String> generateTitle(String firstMessage) {
        try {
            String prompt = "Generate a very short, 2-4 word title for a chat conversation that begins with this message: \""
                    + firstMessage + "\". Return ONLY the title text, no quotes or explanation.";

            return generateResponse(prompt).thenApply(title -> {
                title = title.replace("\"", "").replace("*", "").trim();
                if (title.length() > 30) {
                    title = title.substring(0, 27) + "...";
                }
                return title;
            });
        } catch (Exception e) {
            log.error("Error generating title", e);
            return CompletableFuture.completedFuture("New Chat");
        }
    }
}
