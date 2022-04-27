package com.example.userservice.controller;

import com.example.userservice.exception.InvalidPasswordException;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.model.AppUser;
import com.example.userservice.model.UserView;
import com.example.userservice.payload.LoginRequest;
import com.example.userservice.payload.LoginResponse;
import com.example.userservice.security.JWTProvider;
import com.example.userservice.security.PasswordEncoder;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping(path = "user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JWTProvider jwtProvider;

    @PostMapping(path = "register")
    public ResponseEntity<UserView> registerNewUser(@Valid @RequestBody AppUser appUser) {
        AppUser createdUser = userService.addNewUser(appUser);
        UserView userView = new UserView(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(userView);
    }

    @GetMapping(path = "{userId}")
    public ResponseEntity<UserView> getUserById(@PathVariable("userId") Long userId) {
        Optional<AppUser> userOptional = userService.getUserById(userId);
        if (userOptional.isPresent()) {
            UserView userView = new UserView(userOptional.get());
            return ResponseEntity.ok().body(userView);
        } else {
            throw new UserNotFoundException();
        }
    }

    @PostMapping(path = "login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        // Validate login credentials
        if (!userService.isValidLoginRequest(loginRequest.getUsername(), loginRequest.getPassword())) {
            throw new InvalidPasswordException();
        }
        // Retrieve user info
        AppUser appUser = userService.getUserByUsername(loginRequest.getUsername())
                .orElseThrow(UserNotFoundException::new);
        // Generate JWT
        String token = jwtProvider.generateToken(appUser);
        // Set status to online
        // TODO
        // Construct and return response
        LoginResponse loginResponse = new LoginResponse(token, new UserView(appUser));
        return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
    }

    @GetMapping(path = "authenticate")
    public ResponseEntity<?> authenticate(HttpServletRequest request) {
        HttpStatus status =  HttpStatus.OK;
        String token = jwtProvider.getJwtFromRequest(request);
        if (token == null || !jwtProvider.isValidToken(token)) {
            status = HttpStatus.UNAUTHORIZED;
        }
        return new ResponseEntity<>(status);
    }
}
