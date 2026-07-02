package com.invision.web.Invision.service;

import com.invision.web.Invision.model.Conversation;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationMemoryService {
    //store chat in memory so agent has reference for the conversation
    private final Map<Long, Conversation> conversations = new ConcurrentHashMap<>();

    //get user conversation or create no conversation if one does not exist
    public Conversation getConversation(Long userId){
        return conversations.computeIfAbsent( userId, id-> new Conversation());
    }

    //remove conversion from memory before storing summary to db (future feat)
    public void removeConversation(Long userId){
        conversations.remove(userId);
    }
}
