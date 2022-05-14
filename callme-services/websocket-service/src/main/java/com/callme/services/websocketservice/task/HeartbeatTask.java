package com.callme.services.websocketservice.task;

import com.callme.services.websocketservice.messaging.MessagePublisher;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Getter
@AllArgsConstructor
public class HeartbeatTask implements Runnable{
    private final Long userId;
    private final long heartbeatInterval;
    private final MessagePublisher messagePublisher;

    @Override
    public void run() {
        System.out.println("Beginning heartbeat task for user " + userId);
        while (!Thread.currentThread().isInterrupted()) {
            System.out.println("Sending heartbeat for user " + userId);
            messagePublisher.publish("heartbeat", userId.toString());
            try {
                Thread.sleep(heartbeatInterval);
            } catch (InterruptedException e) {
                System.out.println("Heartbeat task for user %d cancelled".formatted(userId));
                break;
            }
        }
    }
}
