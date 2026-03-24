package com.serkowski.service;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;

public class PromptEnrichmentAdvisor implements CallAdvisor {
    private static final String EXTRA_TEXT = "Please provide a detailed and thoughtful response to the user's query.";

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        Prompt augmentedPrompt = chatClientRequest.prompt()
                .augmentUserMessage(userMessage ->
                        userMessage.mutate()
                                .text(userMessage.getText() + "\n" + EXTRA_TEXT)
                                .build()
                );

        ChatClientRequest enrichedRequest = chatClientRequest.mutate()
                .prompt(augmentedPrompt)
                .build();
        return callAdvisorChain.nextCall(enrichedRequest);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
