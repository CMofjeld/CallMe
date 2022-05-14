package com.callme.services.statusservice.messaging;

import com.callme.services.statusservice.model.UserStatus;
import com.callme.services.statusservice.repository.UserStatusRepository;
import com.callme.services.statusservice.service.UserStatusService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {
    private final UserStatusService userStatusService;

    @Override
    @Transactional
    public void onMessage(Message message, byte[] pattern) {
        String body = message.toString();
        try {
            // Parse message body to user ID
            Long userId = Long.parseLong(body);
            System.out.println("Received heartbeat for " + userId);
            // Update status for user
            Optional<UserStatus> statusOptional = userStatusService.getUserStatusById(userId);
            if (statusOptional.isPresent()) {
                UserStatus userStatus = statusOptional.get();
                // If the user was offline, set them to online now
                if (userStatus.getStatus() == "offline") {
                    userStatus.setStatus("online");
                }
                // Save status to update timestamp
                userStatusService.saveUserStatus(userStatus);
            } else {
                // No user with that ID
                System.err.println("No user found with ID %d from heartbeat message".formatted(userId));
            }
        } catch (NumberFormatException e) {
            System.err.println("Unable to parse body of heartbeat message into integer user ID: " + body);
        }
    }
}
