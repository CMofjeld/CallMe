package com.callme.services.statusservice.messaging;

public interface MessagePublisher {
    public void publish(String topic, Object message);
}
