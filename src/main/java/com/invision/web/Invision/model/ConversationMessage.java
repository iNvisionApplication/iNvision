package com.invision.web.Invision.model;


import java.time.Instant;

//message template
public record ConversationMessage(ChatRole chatRole, String content, Instant timestamp) {

    //roles, so we can assign the messages to the correct party
    public enum ChatRole {
        USER,
        ASSISTANT,
        SYSTEM
    }

}
