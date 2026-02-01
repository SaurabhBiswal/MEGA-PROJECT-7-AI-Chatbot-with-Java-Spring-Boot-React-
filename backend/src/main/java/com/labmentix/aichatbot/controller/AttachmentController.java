package com.labmentix.aichatbot.controller;

import com.labmentix.aichatbot.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private com.labmentix.aichatbot.service.DocumentProcessorService documentProcessorService;

    @Autowired
    private com.labmentix.aichatbot.repository.KnowledgeRepository knowledgeRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileStorageService.uploadFile(file);

            // If PDF, extract text and save to Knowledge Base (for immediate context)
            if (file.getContentType() != null && file.getContentType().equals("application/pdf")) {
                try {
                    String text = documentProcessorService.extractTextFromPdf(file);

                    // Save to DB
                    com.labmentix.aichatbot.model.KnowledgeDocument doc = com.labmentix.aichatbot.model.KnowledgeDocument
                            .builder()
                            .fileName(file.getOriginalFilename())
                            .sourceUrl(url)
                            .content(text)
                            .build();
                    knowledgeRepository.save(doc);
                } catch (Exception e) {
                    System.err.println("Failed to extract text from PDF: " + e.getMessage());
                    // Continue without failing the upload
                }
            }

            return ResponseEntity.ok(Map.of(
                    "url", url,
                    "type", file.getContentType(),
                    "name", file.getOriginalFilename()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading file: " + e.getMessage());
        }
    }
}
