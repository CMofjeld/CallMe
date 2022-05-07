package com.callme.services.callservice.messaging;

public interface MessagePublisher {
    public void publish(String topic, Object message);
}
