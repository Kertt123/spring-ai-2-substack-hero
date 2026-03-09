# spring-ai-2-substack-hero

Spring AI 2 step by step - a sample project showing structured response in practice.

## Project goal

This project demonstrates how to build REST endpoints with Spring AI 2 and return structured responses, including a single object and lists of movie recommendations.

## Features

- Chat endpoint returning a text response.
- Streaming responses via Server-Sent Events.
- Structured response as a single object and a list of objects.
- Output schema example in the AI response.

## Requirements

- Java 25 (as in `pom.xml`).
- Maven.
- Ollama running locally (default model: `qwen3`).

## Configuration

Default configuration is in `src/main/resources/application.yaml`:
- `spring.ai.ollama.chat.options.model` - the model used by Ollama.

## Run locally

```bash
./mvnw spring-boot:run
```

## API

All endpoints accept JSON in the form:

```json
{"message":"Your message"}
```

- `POST /chat` - simple text response.
- `POST /chatStream` - streaming response (SSE).
- `POST /movieRecommendation` - structured response (single object).
- `POST /movieRecommendations` - structured response (list of objects).
- `POST /movieRecommendationSchema` - structured response based on output schema.

Example call:

```bash
curl -X POST http://localhost:8080/movieRecommendation \
  -H "Content-Type: application/json" \
  -d '{"message":"Recommend a sci-fi movie for tonight"}'
```
