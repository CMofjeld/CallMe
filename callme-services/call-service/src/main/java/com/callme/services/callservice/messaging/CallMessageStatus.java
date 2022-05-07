package com.callme.services.callservice.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CallMessageStatus {
    offering("offering"),
    accepted("accepted"),
    disconnected("disconnected"),
    declined("declined");

    private String statusName;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CallMessageStatus getStatusFromCode(String statusName) {
        for (CallMessageStatus status : CallMessageStatus.values()) {
            if (status.getStatusName().equals(statusName)) {
                return status;
            }
        }
        return null;
    }
}
