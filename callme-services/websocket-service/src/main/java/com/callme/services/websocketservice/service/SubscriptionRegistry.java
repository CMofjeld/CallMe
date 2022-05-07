package com.callme.services.websocketservice.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SubscriptionRegistry {
    private final ConcurrentHashMap<String, Set<String>> subscriptionsByTopic = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> subscriptionsById = new ConcurrentHashMap<>();

    public void addSubscription(String topic, String sessionId) {
        subscriptionsByTopic.putIfAbsent(topic, ConcurrentHashMap.newKeySet());
        subscriptionsByTopic.get(topic).add(sessionId);
        subscriptionsById.putIfAbsent(sessionId, ConcurrentHashMap.newKeySet());
        subscriptionsById.get(sessionId).add(topic);
    }

    public boolean topicHasSubscriptions(String topic) {
        return subscriptionsByTopic.containsKey(topic) && !subscriptionsByTopic.get(topic).isEmpty();
    }

    public Set<String> getTopicSubscribers(String topic) {
        return subscriptionsByTopic.get(topic);
    }

    public Set<String> getSubscriptionsById(String sessionId) {
        return subscriptionsById.get(sessionId);
    }

    public void removeSubscription(String topic, String sessionId) {
        if (subscriptionsByTopic.containsKey(topic) && subscriptionsById.containsKey(sessionId)) {
            Set<String> topicSubscriptions = subscriptionsByTopic.get(topic);
            topicSubscriptions.remove(sessionId);
            // If there are no remaining subscriptions for the topic,
            // remove it from the registry
            if (topicSubscriptions.isEmpty()) {
                subscriptionsByTopic.remove(topic);
            }
            subscriptionsById.get(sessionId).remove(topic);
        } else {
            System.err.println("Subscription not found.");
        }
    }
}
