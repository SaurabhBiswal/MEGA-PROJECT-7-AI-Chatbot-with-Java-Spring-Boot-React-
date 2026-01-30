package com.labmentix.aichatbot.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@Data
@NoArgsConstructor
public class GeminiResponse {
    private List<Candidate> candidates;

    @Data
    @NoArgsConstructor
    public static class Candidate {
        private Content content;
    }

    @Data
    @AllArgsConstructor // Added as per snippet
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true) // Added as per instruction
    public static class Content {
        private List<Part> parts;
        private String role; // Added as per instruction
    }

    @Data
    @NoArgsConstructor
    public static class Part {
        private String text;
    }
}
