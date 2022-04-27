package com.example.userservice.payload;

import com.example.userservice.model.UserView;
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
