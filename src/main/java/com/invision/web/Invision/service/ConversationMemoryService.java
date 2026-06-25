package com.invision.web.Invision.service;

import com.invision.web.Invision.model.Conversation;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationMemoryService {
    private final Map<Long, Conversation> conversations = new ConcurrentHashMap<>();

    public Conversation getConversation(Long userId){
        return conversations.computeIfAbsent( userId, id-> new Conversation());
    }

    public void removeConversation(Long userId){
        conversations.remove(userId);
    }
}
