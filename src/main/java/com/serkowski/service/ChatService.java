package com.serkowski.service;

import com.serkowski.model.MovieRecommendation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String chat(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    public Flux<String> chatStream(String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }

    public MovieRecommendation movieRecommendation(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .entity(MovieRecommendation.class);
    }

    public List<MovieRecommendation> movieRecommendations(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .entity(new ParameterizedTypeReference<List<MovieRecommendation>>() {
                });
    }

    public MovieRecommendation movieRecommendationViaOutputSchema(String message) {
        var converter = new BeanOutputConverter<>(MovieRecommendation.class);

        String response = chatClient.prompt()
                .options(OllamaChatOptions.builder()
                        .outputSchema(converter.getJsonSchema())
                        .build())
                .user(message)
                .call()
                .content();
        return converter.convert(response);
    }
}
