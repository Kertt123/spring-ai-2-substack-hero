package com.serkowski.service;

import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

    private final ImageModel imageModel;

    public ImageService(ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    public byte[] generateImage(String message) {
        ImageResponse response = imageModel.call(new ImagePrompt(message,
                OpenAiImageOptions.builder()
                        .model("gpt-image-1")
                        .quality("medium")
                        .N(1)
                        .height(1024)
                        .width(1024)
                        .build()));
        String baseResponse = response.getResult().getOutput().getB64Json();
        return java.util.Base64.getDecoder().decode(baseResponse);
    }

}
