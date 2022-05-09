package com.callme.services.common.service;

import org.springframework.stereotype.Service;

@Service
public interface FriendServiceClient {
    public boolean areFriends(Long user1, Long user2);
}
