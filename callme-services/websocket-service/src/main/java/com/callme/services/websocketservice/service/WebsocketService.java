package com.callme.services.websocketservice.service;

import com.callme.services.websocketservice.messaging.RedisSubscriber;
import com.callme.services.websocketservice.model.SessionEntry;
import com.callme.services.websocketservice.model.SubscriptionMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class WebsocketService extends TextWebSocketHandler {
    private final SessionRegistry sessionRegistry;
    private final SubscriptionRegistry subscriptionRegistry;
    private final RedisMessageListenerContainer messageListenerContainer;
    private final MessageListenerAdapter listenerAdapter;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Get the associated session entry
        String sessionId = session.getId();
        // Parse message
        String payload = message.getPayload();
        SubscriptionMessage subscriptionMessage;
        try {
            subscriptionMessage = new ObjectMapper().readValue(payload, SubscriptionMessage.class);
            String topic = subscriptionMessage.getTopic();
            // Determine what action to take
            switch (subscriptionMessage.getAction()) {
                case "subscribe":
                    addSubscription(topic, sessionId);
                    break;
                case "unsubscribe":
                    removeSubscription(topic, sessionId);
                    break;
                default:
                    System.err.println("Unrecognized action: " + subscriptionMessage.getAction());
                    break;
            }
        } catch (JsonProcessingException e) {
            System.err.println("Unable to parse message: " + payload);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Create session entry and store it in the session directory
        System.out.println("after connection established");
        Long userId = (Long) session.getAttributes().get("userId");
        SessionEntry sessionEntry = new SessionEntry(session, userId);
        sessionRegistry.setSession(session.getId(), sessionEntry);
        System.out.println("Added entry for: " + sessionEntry);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        List<String> subscriptions = new ArrayList<>(subscriptionRegistry.getSubscriptionsById(sessionId));
        for (String topic : subscriptions) {
            removeSubscription(topic, sessionId);
        }
        sessionRegistry.removeSession(sessionId);
        System.out.println("Removed entry for: " + sessionId);
    }

    private void addListenerToTopic(String topic) {
        messageListenerContainer.addMessageListener(listenerAdapter, new ChannelTopic(topic));
    }

    private void removeListenerFromTopic(String topic) {
        messageListenerContainer.removeMessageListener(listenerAdapter, new ChannelTopic(topic));
    }

    private void addSubscription(String topic, String sessionId) {
        if (!subscriptionRegistry.topicHasSubscriptions(topic)) {
            addListenerToTopic(topic);
        }
        subscriptionRegistry.addSubscription(topic, sessionId);
    }

    private void removeSubscription(String topic, String sessionId) {
        subscriptionRegistry.removeSubscription(topic, sessionId);
        if (!subscriptionRegistry.topicHasSubscriptions(topic)) {
            removeListenerFromTopic(topic);
        }
    }
}
