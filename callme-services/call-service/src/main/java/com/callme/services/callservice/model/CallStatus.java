package com.callme.services.callservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CallStatus {
    initiating("pending"),
    ongoing("ongoing"),
    completed("completed"),
    declined("declined");

    private String statusName;
}
