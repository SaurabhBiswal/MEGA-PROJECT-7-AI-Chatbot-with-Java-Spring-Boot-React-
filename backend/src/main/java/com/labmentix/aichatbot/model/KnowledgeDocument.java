package com.labmentix.aichatbot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "knowledge_base")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "embedding", columnDefinition = "vector(384)") // all-MiniLM-L6-v2 size
    private double[] embedding;

    private String fileName;

    @Column(columnDefinition = "TEXT")
    private String sourceUrl;
}
