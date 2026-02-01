package com.labmentix.aichatbot.repository;

import com.labmentix.aichatbot.model.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeRepository extends JpaRepository<KnowledgeDocument, Long> {

    @Query(value = "SELECT * FROM knowledge_base ORDER BY embedding <=> CAST(:query_embedding AS vector) LIMIT :top_n", nativeQuery = true)
    List<KnowledgeDocument> findSimilarDocuments(@Param("query_embedding") String queryEmbedding,
            @Param("top_n") int topN);

    java.util.Optional<KnowledgeDocument> findBySourceUrl(String sourceUrl);
}
