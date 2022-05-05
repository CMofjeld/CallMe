package com.callme.services.common.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserStatusMapper {
    private ObjectMapper objectMapper = new ObjectMapper();

    public String serializeUserStatus(UserStatusView userStatusView) throws JsonProcessingException {
        return objectMapper.writeValueAsString(userStatusView);
    }

    public UserStatusView deserializeUserStatus(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, UserStatusView.class);
    }
}
