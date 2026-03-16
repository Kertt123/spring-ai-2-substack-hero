package com.serkowski.controller;

import com.serkowski.model.ChatWithFileRequest;
import com.serkowski.service.ChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody ChatWithFileRequest request) {
        return chatService.chat(request.message(), request.fileUrl());
    }


}
