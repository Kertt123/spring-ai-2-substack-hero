package com.serkowski;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Tag("integration")
class ReRankingEvaluationTest {

    @Autowired
    private ChatClient chatClient;

    private RelevancyEvaluator relevancyEvaluator;
    private FactCheckingEvaluator factCheckingEvaluator;

    @BeforeEach
    void setUp() {
        relevancyEvaluator = new RelevancyEvaluator(ChatClient.builder(getTestModel("qwen3:4b")));

        factCheckingEvaluator = FactCheckingEvaluator.builder(ChatClient.builder(getTestModel("bespoke-minicheck")))
                .build();
    }

    @ParameterizedTest
    @MethodSource("dysonQuestions")
    void withReranking_shouldPassBothEvaluators(String question) {
        ChatResponse response = chatClient.prompt()
                .user(question)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "123"))
                .call()
                .chatResponse();

        String answer = response.getResult().getOutput().getText();
        List<Document> docs = response.getMetadata()
                .get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT);

        EvaluationRequest request = new EvaluationRequest(question, docs, answer);

        assertThat(relevancyEvaluator.evaluate(request).isPass())
                .as("Answer should be relevant: " + question)
                .isTrue();

        assertThat(factCheckingEvaluator.evaluate(request).isPass())
                .as("Answer should be factually correct: " + question)
                .isTrue();
    }

    static Stream<String> dysonQuestions() {
        return Stream.of(
                "What should I check if my Dyson V10 stops working?",
                "How do I clean the filter on my Dyson V10?",
                "What does a pulsing blue LED mean on the Dyson V10?",
                "How do I empty the dust bin on the Dyson V10?"
        );
    }

    private static OllamaChatModel getTestModel(String model) {
        return OllamaChatModel.builder()
                .ollamaApi(OllamaApi.builder()
                        .restClientBuilder(getRestClientWithExtendedTimeouts())
                        .build())
                .options(OllamaChatOptions.builder()
                        .model(model)
                        .temperature(0.0)
                        .build())
                .build();
    }

    private static RestClient.Builder getRestClientWithExtendedTimeouts() {
        return RestClient.builder()
                .requestFactory(new ReactorClientHttpRequestFactory(
                        HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(300))
                ))
                .baseUrl("http://localhost:11434");
    }
}