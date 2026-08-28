package com.serkowski.configuration;

import com.serkowski.service.VectorStoreService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;

import java.util.List;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(30)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory, VectorStore vectorStore) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory)
                                .build(),
//                        QuestionAnswerAdvisor.builder(vectorStore)
//                                .searchRequest(SearchRequest.builder()
//                                        .topK(4)
//                                        .similarityThreshold(0.3)
//                                        .build())
//                                .build()
                        RetrievalAugmentationAdvisor.builder()
                                .queryTransformers(RewriteQueryTransformer.builder()
                                        .chatClientBuilder(ChatClient.builder(chatModel))
                                        .build())
                                .queryExpander(MultiQueryExpander.builder()
                                        .chatClientBuilder(ChatClient.builder(chatModel))
                                        .numberOfQueries(3)
                                        .includeOriginal(true)
                                        .build())
                                .queryAugmenter(ContextualQueryAugmenter.builder()
                                        .allowEmptyContext(true)
                                        .build())
                                .documentRetriever(VectorStoreDocumentRetriever.builder()
                                        .vectorStore(vectorStore)
                                        .similarityThreshold(0.5)
                                        .topK(5)
                                        .build())
                                .build()
                )
                .build();
    }

    @Bean
    public VectorStoreService vectorStoreService(VectorStore vectorStore, KeywordMetadataEnricher keywordMetadataEnricher, SummaryMetadataEnricher summaryMetadataEnricher) {
        return new VectorStoreService(vectorStore, keywordMetadataEnricher, summaryMetadataEnricher);
    }

    @Bean
    public KeywordMetadataEnricher keywordEnricher() {
        OllamaChatModel enrichmentModel = OllamaChatModel.builder()
                .ollamaApi(OllamaApi.builder()
                        .baseUrl("http://localhost:11434")
                        .build())
                .options(OllamaChatOptions.builder()
                        .model("qwen3:4b")
                        .temperature(0.0)
                        .build())
                .build();
        return KeywordMetadataEnricher.builder(enrichmentModel)
                .keywordsTemplate(new PromptTemplate(
                        "Extract 5 important technical keywords from the following text. " +
                                "Focus on product names, components, and actions. " +
                                "Format as comma separated.\n{context_str}"))
                .build();
    }

    @Bean
    public SummaryMetadataEnricher summaryEnricher(ChatModel chatModel) {
        return new SummaryMetadataEnricher(chatModel,
                List.of(
                        SummaryMetadataEnricher.SummaryType.PREVIOUS,
                        SummaryMetadataEnricher.SummaryType.CURRENT,
                        SummaryMetadataEnricher.SummaryType.NEXT
                ));
    }
}
