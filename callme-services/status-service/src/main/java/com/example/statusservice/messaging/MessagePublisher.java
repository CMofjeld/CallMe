package com.example.statusservice.messaging;

import com.example.statusservice.model.UserStatus;

public interface MessagePublisher {
    public void publish(String topic, Object message);
}
