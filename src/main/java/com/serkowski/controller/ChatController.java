package com.serkowski.controller;

import com.serkowski.model.MovieRecommendation;
import com.serkowski.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody TextRequest request) {
        return chatService.chat(request.message);
    }

    @PostMapping(value = "/chatStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody TextRequest request) {
        return chatService.chatStream(request.message);
    }

    @PostMapping("/movieRecommendation")
    public MovieRecommendation movieRecommendation(@RequestBody TextRequest request) {
        return chatService.movieRecommendation(request.message);
    }

    @PostMapping("/movieRecommendations")
    public List<MovieRecommendation> movieRecommendations(@RequestBody TextRequest request) {
        return chatService.movieRecommendations(request.message);
    }

    @PostMapping("/movieRecommendationSchema")
    public MovieRecommendation movieRecommendationSchema(@RequestBody TextRequest request) {
        return chatService.movieRecommendationViaOutputSchema(request.message);
    }


    public record TextRequest(String message) {
    }
}
