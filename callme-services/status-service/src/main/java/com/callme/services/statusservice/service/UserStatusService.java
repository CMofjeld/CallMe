package com.callme.services.statusservice.service;

import com.callme.services.statusservice.model.UserStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserStatusService {
    public Optional<UserStatus> getUserStatusById(Long id);
    public boolean saveUserStatus(UserStatus userStatus);
    public void deleteUserStatus(Long id);
}
