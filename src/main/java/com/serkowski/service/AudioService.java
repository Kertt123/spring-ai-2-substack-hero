package com.serkowski.service;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AudioService {

    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final TextToSpeechModel ttsModel;

    public AudioService(OpenAiAudioTranscriptionModel transcriptionModel, TextToSpeechModel ttsModel) {
        this.transcriptionModel = transcriptionModel;
        this.ttsModel = ttsModel;
    }

    public String transcribe(Resource fileResource) {
        OpenAiAudioTranscriptionOptions audioOptions = OpenAiAudioTranscriptionOptions.builder()
                .language("en")
                .temperature(0f)
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .build();
        AudioTranscriptionResponse response = transcriptionModel.call(new AudioTranscriptionPrompt(fileResource, audioOptions));
        return response.getResult().getOutput();
    }

    public byte[] speech(String message) {
        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                .model("gpt-4o-mini-tts")
                .voice(OpenAiAudioApi.SpeechRequest.Voice.CORAL)
                .speed(1.0)
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .build();

        TextToSpeechResponse response = ttsModel.call(new TextToSpeechPrompt(message, options));

        return response.getResult().getOutput();
    }

    public Flux<byte[]> speechStream(String message) {
        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                .model("gpt-4o-mini-tts")
                .voice(OpenAiAudioApi.SpeechRequest.Voice.CORAL)
                .speed(1.0)
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .build();

        return ttsModel.stream(new TextToSpeechPrompt(message, options))
                .map(response -> response.getResult().getOutput());
    }
}
