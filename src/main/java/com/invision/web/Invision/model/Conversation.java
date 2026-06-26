package com.invision.web.Invision.model;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
    List<ConversationMessage> messages = new ArrayList<>();
    private static final int MAX_MESSAGES = 30;

    public void addChatMessage(ConversationMessage conversationMessage){
        messages.add(conversationMessage);
        if (messages.size() > MAX_MESSAGES) {
            messages.removeFirst();
        }
    }

    public List<ConversationMessage> getMessages(){
        return List.copyOf(messages);
    }

    public void clearMessages(){
        messages.clear();
    }
}
