package com.example.statusservice.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class UserStatusView {
    @NotBlank
    private String id;
    @NotBlank
    private String status;

    public UserStatusView(UserStatus userStatus) {
        this.id = userStatus.getId();
        this.status = userStatus.getStatus();
    }
}
