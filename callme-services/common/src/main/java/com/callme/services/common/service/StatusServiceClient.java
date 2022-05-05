package com.callme.services.common.service;

import com.callme.services.common.model.UserStatusView;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface StatusServiceClient {
    public boolean setUserStatus(UserStatusView userStatusView);
    public Optional<UserStatusView> getUserStatus(Long id);
}
