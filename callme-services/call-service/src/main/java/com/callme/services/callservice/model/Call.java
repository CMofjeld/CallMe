package com.callme.services.callservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@RedisHash("Call")
@AllArgsConstructor
@NoArgsConstructor
public class Call {
    private String id;
    @NotNull
    private Long caller;
    @NotNull
    private Long receiver;
    private CallStatus status;
    private LocalDateTime startedAt;

    public Call(Long caller, Long receiver) {
        this.caller = caller;
        this.receiver = receiver;
        this.id = userIdsToCallId(caller, receiver);
        this.status = CallStatus.initiating;
        this.startedAt = LocalDateTime.now();
    }

    public static String userIdsToCallId(Long caller, Long receiver) {
        return "%d.%d".formatted(caller, receiver);
    }
}
