package com.callme.services.websocketservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ClientMessage {
    private String topic;
    private String body;
}
