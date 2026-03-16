# spring-ai-2-substack-hero

Spring AI step by step.

This demo project showcases how to integrate Generative AI models into a Spring Boot application, leveraging **Spring WebFlux** for efficient data streaming.

## Key Features

The application demonstrates practical examples of four key AI modalities:

*   **TTI (Text-to-Image):** Generating images from text descriptions (using DALL-E 3).
*   **ITT (Image-to-Text / Vision):** Analyzing images and generating text descriptions using multimodal models.
*   **TTS (Text-to-Speech):** Synthesizing speech from text. Thanks to WebFlux, the application supports real-time audio **streaming** (model tts-1).
*   **STT (Speech-to-Text):** Transcribing audio files to text (model Whisper).

## Technical Requirements

*   Java 17+
*   Maven
*   OpenAI API Key set as an environment variable `OPENAI_API_KEY`

> **Note:** The project requires the `spring-boot-starter-webflux` dependency instead of the classic `spring-boot-starter-web` to correctly handle reactive types (`Flux`) returned by AI services (especially for audio streaming).
