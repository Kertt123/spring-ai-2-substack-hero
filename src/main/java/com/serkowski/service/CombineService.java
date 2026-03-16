package com.serkowski.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
public class CombineService {

    private final ChatService chatService;
    private final ImageService imageService;
    private final AudioService audioService;

    public CombineService(ChatService chatService, ImageService imageService, AudioService audioService) {
        this.chatService = chatService;
        this.imageService = imageService;
        this.audioService = audioService;
    }

    public byte[] combine(String message) {
        byte[] image = imageService.generateImage(message);

        String describeResponse = chatService.chatWithResource("Lets describe the image in 3 sentences", new ByteArrayResource(image));

        return audioService.speech(describeResponse);
    }

}
