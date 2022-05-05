package com.callme.services.common.service;

import org.springframework.stereotype.Service;

@Service
public interface UserServiceClient {
    public boolean userExists(Long id);
}
