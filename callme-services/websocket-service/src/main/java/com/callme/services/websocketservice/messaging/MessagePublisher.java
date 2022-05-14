package com.callme.services.websocketservice.messaging;

public interface MessagePublisher {
    public void publish(String topic, Object message);
}
