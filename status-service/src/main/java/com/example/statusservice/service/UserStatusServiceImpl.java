package com.example.statusservice.service;

import com.example.statusservice.model.UserStatus;
import com.example.statusservice.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserStatusServiceImpl implements UserStatusService {
    private final UserStatusRepository userStatusRepository;
    @Value("${USER_STATUS_TTL}")
    private Long userStatusTTL;

    @Override
    public Optional<UserStatus> getUserStatusById(String id) {
        // TODO validated user ID
        Optional<UserStatus> optionalUserStatus = userStatusRepository.findById(id);
        if (optionalUserStatus.isPresent()) {
            // Status for that user found
            // Check if status info has expired
            Date now = new Date();
            UserStatus userStatus = optionalUserStatus.get();
            Long lastUpdated = userStatus.getUpdatedAt();
            if (now.getTime() - lastUpdated > userStatusTTL) {
                // Delete from the repo and return empty
                userStatusRepository.delete(userStatus);
                return Optional.empty();
            }
            // Status not expired - return current value
            return optionalUserStatus;
        }
        // User status not found - return empty
        return Optional.empty();
    }

    @Override
    public boolean saveUserStatus(UserStatus userStatus) {
        // TODO check that user ID is valid
        if (userStatus.getStatus().equals("offline")) {
            // Can remove existing entry to indicate offline
            userStatusRepository.delete(userStatus);
        } else {
            userStatusRepository.save(userStatus);
        }
        return true;
    }

    @Override
    public void deleteUserStatus(String id) {
        userStatusRepository.deleteById(id);
    }
}
