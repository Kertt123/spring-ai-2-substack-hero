package com.serkowski.service;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

public class ShrekGuardAdvisor implements BaseAdvisor {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        var chatResponse = chatClientResponse.chatResponse();
        if (chatResponse == null || chatResponse.getResult() == null) {
            return chatClientResponse;
        }
        String originalText = chatResponse.getResult().getOutput().getText();
        if (originalText == null) {
            return chatClientResponse;
        }
        String maskedText = originalText.replaceAll("(?i)Shrek|Fiona|Donkey", "[HIDDEN]");
        if (!originalText.equals(maskedText)) {
            var maskedOutput = AssistantMessage.builder()
                    .content(maskedText)
                    .properties(chatResponse.getResult().getOutput().getMetadata())
                    .build();
            var maskedGeneration = new Generation(maskedOutput, chatResponse.getResult().getMetadata());
            var maskedChatResponse = new ChatResponse(java.util.List.of(maskedGeneration), chatResponse.getMetadata());
            return chatClientResponse.mutate().chatResponse(maskedChatResponse).build();
        }
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
