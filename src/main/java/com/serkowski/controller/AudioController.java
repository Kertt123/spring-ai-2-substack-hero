package com.serkowski.controller;

import com.serkowski.model.TextRequest;
import com.serkowski.service.AudioService;
import com.serkowski.service.CombineService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/audio")
public class AudioController {

    private final AudioService audioService;
    private final CombineService combineService;

    public AudioController(AudioService audioService, CombineService combineService) {
        this.audioService = audioService;
        this.combineService = combineService;
    }

    @PostMapping(value = "/transcribe")
    public String transcribe(@RequestParam("file") MultipartFile audioFile) {
        return audioService.transcribe(audioFile.getResource());
    }

    @PostMapping(value = "/speech", produces = "audio/mpeg")
    public byte[] speech(@RequestBody TextRequest textRequest) {
        return audioService.speech(textRequest.message());
    }

    @PostMapping(value = "/speechStream", produces = "audio/mpeg")
    public Flux<byte[]> speechStream(@RequestBody TextRequest textRequest) {
        return audioService.speechStream(textRequest.message());
    }

    @PostMapping(value = "/combine", produces = "audio/mpeg")
    public byte[] combine(@RequestBody TextRequest textRequest) {
        return combineService.combine(textRequest.message());
    }

}
