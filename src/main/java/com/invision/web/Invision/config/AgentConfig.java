package com.invision.web.Invision.config;

import com.invision.web.Invision.tool.InvisionTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AgentConfig {

    private final InvisionTools invisionTools;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                You are Ivy, an intelligent assistant for the Invision Asset Management System.
                You help staff with finding assets, loan workflows, and system guidance.
                For any question about specific assets or loans, always call the appropriate 
                function — never guess or make up asset names, statuses, or loan details.
                Keep responses short, friendly, and professional.
                """)
                .defaultTools(invisionTools)
                .build();
    }

}
