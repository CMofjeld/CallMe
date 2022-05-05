package com.callme.services.friendservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RelationshipStatus {
    PENDING("pending"),
    ACCEPTED("accepted"),
    DECLINED("declined");

    private String statusName;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RelationshipStatus getStatusFromCode(String statusName) {
        for (RelationshipStatus status : RelationshipStatus.values()) {
            if (status.getStatusName().equals(statusName)) {
                return status;
            }
        }
        return null;
    }
}
