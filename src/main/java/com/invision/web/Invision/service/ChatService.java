package com.invision.web.Invision.service;

import com.invision.web.Invision.config.CustomUserDetails;
import com.invision.web.Invision.model.ConversationMessage;
import com.invision.web.Invision.model.Conversation;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatClient chatClient;
    private final ConversationMemoryService memoryService;

    public String chat(String userMessage){
        Long userId = getCurrentUserId();

        //get conversation from memory or create new conversation if one does not exist
        Conversation conversation = memoryService.getConversation(userId);

        //add new user message to conversation memory
        conversation.addChatMessage(
                new ConversationMessage(
                        ConversationMessage.ChatRole.USER,
                        userMessage,
                        Instant.now()
                ));

        //convert conversation to list of spring ai messages
        Prompt prompt = new Prompt(buildMessages(conversation));

        //get ai response
        var chatResponse = chatClient.prompt(prompt)
                .call()
                .chatResponse();

        //add assistant response to the conversation
        String response = chatResponse.getResult().getOutput().getText();
                  conversation.addChatMessage(
                new ConversationMessage(
                        ConversationMessage.ChatRole.ASSISTANT,
                        response,
                        Instant.now()
                )
        );

        return response;

    }

    public Long getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new IllegalStateException("No authenticated user found.");

    }

    //convert conversation to list of spring ai messages
    private List<Message> buildMessages(Conversation conversation) {

        List<Message> messages = new ArrayList<>();

        //add messages from conversation to list of spring ai messages with roles
        for (ConversationMessage message : conversation.getMessages()) {

            switch (message.chatRole()) {

                case USER ->
                        messages.add(new UserMessage(message.content()));

                case ASSISTANT ->
                        messages.add(new AssistantMessage(message.content()));
            }
        }

        return messages;
    }
}
