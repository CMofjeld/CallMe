package com.callme.services.websocketservice.messaging;

import com.callme.services.websocketservice.model.ClientMessage;
import com.callme.services.websocketservice.model.SessionEntry;
import com.callme.services.websocketservice.service.SessionRegistry;
import com.callme.services.websocketservice.service.SubscriptionRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@AllArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {
    private final SessionRegistry sessionRegistry;
    private final SubscriptionRegistry subscriptionRegistry;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // Convert to client format
        String topic = new String(message.getChannel(), StandardCharsets.UTF_8);
        System.out.println("Received message from topic %s".formatted(topic));
        String body = message.toString();
        TextMessage clientMessage;
        try {
            clientMessage = new TextMessage(
                    objectMapper.writeValueAsString(
                            new ClientMessage(topic, body)
                    )
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // Find subscribed clients
        Set<String> topicSubscribers = subscriptionRegistry.getTopicSubscribers(topic);
        // Republish message to them
        for (String sessionId : topicSubscribers) {
            SessionEntry sessionEntry = sessionRegistry.getSession(sessionId).orElseThrow();
            Long userId = sessionEntry.getUserId();
            try {
                System.out.println("Forwarding message to user %d".formatted(userId));
                sessionEntry.getSession().sendMessage(clientMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
