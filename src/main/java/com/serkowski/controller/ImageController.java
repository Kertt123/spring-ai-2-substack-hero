package com.serkowski.controller;

import com.serkowski.model.TextRequest;
import com.serkowski.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/image")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] generateImage(@RequestBody TextRequest request) {
        return imageService.generateImage(request.message());
    }

}
