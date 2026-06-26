package com.invision.web.Invision.model;


import java.time.Instant;

public record ConversationMessage(ChatRole chatRole, String content, Instant timestamp) {

    public enum ChatRole {
        USER,
        ASSISTANT,
        SYSTEM
    }

}
