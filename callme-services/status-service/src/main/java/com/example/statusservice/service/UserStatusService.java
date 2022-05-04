package com.example.statusservice.service;

import com.example.statusservice.model.UserStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserStatusService {
    public Optional<UserStatus> getUserStatusById(String id);
    public boolean saveUserStatus(UserStatus userStatus);
    public void deleteUserStatus(String id);
}
