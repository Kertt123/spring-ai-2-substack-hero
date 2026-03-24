package com.serkowski.configuration;

import com.serkowski.model.Response;
import com.serkowski.service.PromptEnrichmentAdvisor;
import com.serkowski.service.ShrekGuardAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor(
                                request -> request.prompt().getUserMessage().getText(),
                                response -> response.getResult().getOutput().getText(),
                                0),
                        new PromptEnrichmentAdvisor(),
                        new ShrekGuardAdvisor(),
                        StructuredOutputValidationAdvisor.builder()
                                .maxRepeatAttempts(3)
                                .outputType(Response.class)
                                .build()
                )
                .build();
    }
}
