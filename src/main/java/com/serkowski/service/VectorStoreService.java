package com.serkowski.service;

import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;

import java.util.List;

public class VectorStoreService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter;

    public VectorStoreService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
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

        vectorStore.accept(splitDocuments);
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
