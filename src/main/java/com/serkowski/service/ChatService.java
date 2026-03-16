package com.serkowski.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;
import java.net.URL;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String chat(String message, String fileUrl) {
        try {
            UrlResource resource = new UrlResource(fileUrl);
            return chatClient.prompt()
                    .user(userSpec -> userSpec.text(message)
                            .media(MimeTypeUtils.IMAGE_PNG, resource))
                    .call()
                    .content();
        } catch (Exception e) {
            throw new IllegalArgumentException("Issue with chat with file " + fileUrl, e);
        }
    }

    public String chatWithResource(String message, Resource file) {
        return chatClient.prompt()
                .user(userSpec -> userSpec.text(message)
                        .media(MimeTypeUtils.IMAGE_PNG, file))
                .call()
                .content();
    }

}
