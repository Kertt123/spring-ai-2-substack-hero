package com.serkowski.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;

import java.util.Comparator;
import java.util.List;

public class LlmRerankerDocumentPostProcessor implements DocumentPostProcessor {

    private final ChatClient chatClient;
    private final int topN;

    public LlmRerankerDocumentPostProcessor(ChatClient chatClient, int topN) {
        this.chatClient = chatClient;
        this.topN = topN;
    }

    @Override
    public List<Document> process(Query query, List<Document> documents) {
        if (documents.isEmpty()) {
            return documents;
        }

        return documents.stream()
                .map(doc -> scoreDocument(query.text(), doc))
                .sorted(Comparator.comparingInt(ScoredDocument::score).reversed())
                .limit(topN)
                .map(ScoredDocument::document)
                .toList();
    }

    private ScoredDocument scoreDocument(String query, Document document) {
        Score score = chatClient.prompt()
                .user(u -> u.text("""
                                On a scale from 1 to 10, how relevant is the following document 
                                to answering the query? 
                                Respond with a single integer only, no explanation.
                                
                                Query: {query}
                                
                                Document: {document}
                                """)
                        .param("query", query)
                        .param("document", document.getText()))
                .call()
                .entity(Score.class);

        return new ScoredDocument(document, score.score);
    }

    record ScoredDocument(Document document, Integer score) {
    }

    record Score(int score) {

    }
}