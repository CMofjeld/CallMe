package com.callme.services.statusservice.service;

import com.callme.services.common.model.UserStatusView;
import com.callme.services.common.service.UserServiceClient;
import com.callme.services.statusservice.messaging.MessagePublisher;
import com.callme.services.statusservice.model.UserStatus;
import com.callme.services.statusservice.repository.UserStatusRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserStatusServiceImpl implements UserStatusService {
    private final UserStatusRepository userStatusRepository;
    private final MessagePublisher messagePublisher;
    private final UserServiceClient userServiceClient;
    @Value("${USER_STATUS_TTL}")
    private Long userStatusTTL;

    private void logDBQuery(String queryDescription) {
        System.out.println("Querying redis to %s".formatted(queryDescription));
    }

    private void logDBStore(String storedObject) {
        System.out.println("Storing %s in redis".formatted(storedObject));
    }

    @Override
    public Optional<UserStatus> getUserStatusById(Long id) {
        // Validate User ID
        if (!userServiceClient.userExists(id)) {
            return Optional.empty();
        }

        logDBQuery("retrieve status for user %d".formatted(id));
        Optional<UserStatus> optionalUserStatus = userStatusRepository.findById(id);
        if (optionalUserStatus.isPresent()) {
            // Status for that user found
            // Check if status info has expired
            Date now = new Date();
            UserStatus userStatus = optionalUserStatus.get();
            Long lastUpdated = userStatus.getUpdatedAt();
            if (now.getTime() - lastUpdated > userStatusTTL) {
                // Delete from the repo and set status to offline
                userStatusRepository.delete(userStatus);
                userStatus.setStatus("offline");
            }
            // Status not expired - return current value
            return optionalUserStatus;
        } else {
            // User status not found - indicate that user is offline
            UserStatus userStatus = new UserStatus(id, "offline");
            return Optional.of(userStatus);
        }
    }

    @Override
    public boolean saveUserStatus(UserStatus userStatus) {
        // Validate User ID
        if (!userServiceClient.userExists(userStatus.getId())) {
            return false;
        }

        logDBStore("user's status");
        if (userStatus.getStatus().equals("offline")) {
            // Can remove existing entry to indicate offline
            userStatusRepository.delete(userStatus);
        } else {
            userStatus.setUpdatedAt(new Date().getTime());
            userStatusRepository.save(userStatus);
        }
        publishUserStatus(userStatus);
        return true;
    }

    @Override
    public void deleteUserStatus(Long id) {
        userStatusRepository.deleteById(id);
    }

    private void publishUserStatus(UserStatus userStatus) {
        try {
            UserStatusView userStatusView = new UserStatusView(userStatus.getId(), userStatus.getStatus());
            String statusMessage = new ObjectMapper().writeValueAsString(userStatusView);
            System.out.println("Publishing message that user %d's status is %s".formatted(
                    userStatus.getId(),
                    userStatus.getStatus()
            ));
            messagePublisher.publish("status." + userStatus.getId(), statusMessage);
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing user status to JSON.");
            System.err.println(e.toString());
        }
    }
}
