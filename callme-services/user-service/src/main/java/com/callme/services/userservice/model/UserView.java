package com.callme.services.userservice.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserView {
    private Long id;
    private String username;

    public UserView(AppUser appUser) {
        this.id = appUser.getId();
        this.username = appUser.getUsername();
    }
}
