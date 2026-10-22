package com.serkowski.retriver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HybridDocumentRetriever implements DocumentRetriever {

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.search.fts-language:english}")
    private String ftsLanguage;

    @Value("${app.search.top-k:5}")
    private int topK;

    @Value("${app.search.similarity-threshold:0.3}")
    private double similarityThreshold;

    private static final int RRF_K = 60;

    public HybridDocumentRetriever(VectorStore vectorStore,
                                   JdbcTemplate jdbcTemplate) {
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Document> retrieve(Query query) {
        // 1. Semantic search via Spring AI VectorStore
        List<Document> semanticResults = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query.text())
                        .topK(topK * 2)
                        .similarityThreshold(similarityThreshold)
                        .build()
        );

        // 2. Keyword search via PostgreSQL full-text search
        List<Document> keywordResults = keywordSearch(query.text(), topK * 2);

        // 3. Combine with Reciprocal Rank Fusion
        return reciprocalRankFusion(semanticResults, keywordResults, topK);
    }

    private List<Document> keywordSearch(String query, int limit) {
        // websearch_to_tsquery handles natural language gracefully:
        // - supports phrases and OR logic
        // - safe for special characters
        // - doesn't fail on single-word misses like plainto_tsquery (AND logic)
        String sql = """
                SELECT id, content, metadata
                FROM vector_store
                WHERE fts_vector @@ websearch_to_tsquery(CAST(? AS regconfig), ?)
                ORDER BY ts_rank(fts_vector, websearch_to_tsquery(CAST(? AS regconfig), ?)) DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new Document(
                        rs.getString("id"),
                        rs.getString("content"),
                        parseMetadata(rs.getString("metadata"))
                ),
                ftsLanguage, query, ftsLanguage, query, limit);
    }

    private List<Document> reciprocalRankFusion(List<Document> semanticResults,
                                                List<Document> keywordResults,
                                                int topN) {
        Map<String, Double> scores = new HashMap<>();
        Map<String, Document> documents = new HashMap<>();

        // Score semantic results
        for (int i = 0; i < semanticResults.size(); i++) {
            Document doc = semanticResults.get(i);
            scores.merge(doc.getId(), 1.0 / (RRF_K + i + 1), Double::sum);
            documents.put(doc.getId(), doc);
        }

        // Score keyword results
        for (int i = 0; i < keywordResults.size(); i++) {
            Document doc = keywordResults.get(i);
            scores.merge(doc.getId(), 1.0 / (RRF_K + i + 1), Double::sum);
            // Safe to overwrite — same document id = same content,
            // just arrived from a different search path
            documents.put(doc.getId(), doc);
        }

        // Sort by combined RRF score and return topN
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topN)
                .map(entry -> documents.get(entry.getKey()))
                .toList();
    }

    private Map<String, Object> parseMetadata(String metadataJson) {
        try {
            return new ObjectMapper().readValue(metadataJson,
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (Exception e) {
            return Map.of();
        }
    }
}