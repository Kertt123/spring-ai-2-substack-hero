package com.serkowski.service;

import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;

import java.util.List;

public class VectorStoreService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter;
    private final KeywordMetadataEnricher keywordMetadataEnricher;
    private final SummaryMetadataEnricher summaryMetadataEnricher;

    public VectorStoreService(VectorStore vectorStore, KeywordMetadataEnricher keywordMetadataEnricher, SummaryMetadataEnricher summaryMetadataEnricher) {
        this.vectorStore = vectorStore;
        this.keywordMetadataEnricher = keywordMetadataEnricher;
        this.summaryMetadataEnricher = summaryMetadataEnricher;
        tokenTextSplitter = TokenTextSplitter.builder()
                .withChunkSize(1000)
                .withMinChunkSizeChars(400)
                .withMinChunkLengthToEmbed(10)
                .withMaxNumChunks(5000)
                .withKeepSeparator(true)
                .build();
    }

    public void storeAsVector(Resource file) {
        List<Document> documents = getDocuments(file);

        List<Document> splitDocuments = tokenTextSplitter.apply(documents);

        List<Document> withKeywords = keywordMetadataEnricher.apply(splitDocuments);
        List<Document> fullyEnriched = summaryMetadataEnricher.apply(withKeywords);

        vectorStore.accept(fullyEnriched);
    }

    private static @NonNull List<Document> getDocuments(Resource file) {
        List<Document> documents;
        if (file.getFilename().toLowerCase().endsWith(".pdf")) {
            TikaDocumentReader pdfReader = new TikaDocumentReader(file);
            documents = pdfReader.get();
        } else {
            TextReader textReader = new TextReader(file);
            documents = textReader.get();
        }
        documents.forEach(document -> {
            document.getMetadata().put("filename", file.getFilename());
        });
        return documents;
    }
}
