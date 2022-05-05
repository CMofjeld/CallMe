package com.callme.services.userservice.payload;

import com.callme.services.common.model.UserView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private UserView user;
}
