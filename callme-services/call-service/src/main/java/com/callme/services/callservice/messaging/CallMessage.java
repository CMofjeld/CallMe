package com.callme.services.callservice.messaging;

import com.callme.services.callservice.model.CallStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CallMessage {
    @NotNull
    private Long userId;
    @NotEmpty
    private String callId;
    @NotNull
    private CallMessageStatus status;
    private String handshakeInfo;

    public CallMessage(Long userId, String callId, CallMessageStatus status) {
        this.userId = userId;
        this.callId = callId;
        this.status = status;
    }
}
