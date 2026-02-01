package com.labmentix.aichatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labmentix.aichatbot.dto.OpenAIRequest;
import com.labmentix.aichatbot.dto.OpenAIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AiServiceImpl() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Async
    public CompletableFuture<String> generateResponse(String userMessage) {
        return generateResponse(userMessage, null, null);
    }

    @Override
    @Async
    public CompletableFuture<String> generateResponse(String userMessage, String attachmentUrl, String attachmentType) {
        return generateResponse(userMessage, attachmentUrl, attachmentType, null);
    }

    @Override
    @Async
    public CompletableFuture<String> generateResponse(String userMessage, String attachmentUrl, String attachmentType,
            String extractedText) {
        try {
            log.info("Searching context for message: {}", userMessage);
            String context = "";
            try {
                context = knowledgeBaseService.searchContext(userMessage);
            } catch (Exception e) {
                log.warn("Knowledge base search failed, proceeding with standard chat: {}", e.getMessage());
            }

            String enhancedPrompt = userMessage;
            if (context != null && !context.isEmpty()) {
                enhancedPrompt = "Use the following context to answer the user's question. If the answer is not in the context, use your general knowledge but mention that it's not in the documents.\n\n"
                        + "CONTEXT:\n" + context + "\n\n"
                        + "USER QUESTION: " + userMessage;
            }

            if (attachmentUrl != null && !attachmentUrl.isEmpty()) {
                if (extractedText != null && !extractedText.isEmpty()) {
                    // We have the actual PDF content!
                    enhancedPrompt += "\n\n[DOCUMENT CONTENT FROM ATTACHED FILE]:\n" + extractedText
                            + "\n[END OF DOCUMENT]\n\n";
                    enhancedPrompt += "The above is the full text content extracted from the user's attached PDF file. Please analyze it based on the user's request.";
                } else {
                    enhancedPrompt += "\n\n[SYSTEM NOTE: The user has attached a file at " + attachmentUrl
                            + ". The file content could not be extracted. Please acknowledge the attachment and ask the user to describe it or add it to the Knowledge Base for analysis.]";
                }
            }

            log.info("Sending request to Groq with context attached");

            // Create Groq Request (OpenAI Compatible)
            // Using llama-3.3-70b-versatile for high performance
            OpenAIRequest.Message message = new OpenAIRequest.Message("user", enhancedPrompt);
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
