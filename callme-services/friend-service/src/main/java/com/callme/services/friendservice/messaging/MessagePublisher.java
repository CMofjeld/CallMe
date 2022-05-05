package com.callme.services.friendservice.messaging;

public interface MessagePublisher {
    public void publish(String topic, Object message);
}
