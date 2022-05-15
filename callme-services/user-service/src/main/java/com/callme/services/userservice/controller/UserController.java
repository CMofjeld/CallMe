package com.callme.services.userservice.controller;

import com.callme.services.common.model.UserStatusView;
import com.callme.services.common.model.UserView;
import com.callme.services.common.service.StatusServiceClient;
import com.callme.services.userservice.exception.InvalidPasswordException;
import com.callme.services.userservice.exception.UserNotFoundException;
import com.callme.services.userservice.model.AppUser;
import com.callme.services.userservice.payload.LoginRequest;
import com.callme.services.userservice.payload.LoginResponse;
import com.callme.services.userservice.security.JWTProvider;
import com.callme.services.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    private final StatusServiceClient statusServiceClient;

    private void logReceivedRequest(String requestAction) {
        System.out.println("Received request to " + requestAction);
    }

    private void logResponse(String responseContent) {
        System.out.println("Returning response with " + responseContent);
    }

    @PostMapping(path = "register")
    public ResponseEntity<UserView> registerNewUser(@Valid @RequestBody AppUser appUser) {
        logReceivedRequest("register user with name %s".formatted(appUser.getUsername()));
        AppUser createdUser = userService.addNewUser(appUser);
        UserView userView = new UserView(appUser.getId(), appUser.getUsername());
        logResponse("registered user");
        return ResponseEntity.status(HttpStatus.CREATED).body(userView);
    }

    @GetMapping(path = "{userId}")
    public ResponseEntity<UserView> getUserById(@PathVariable("userId") Long userId) {
        logReceivedRequest("get user with ID %d".formatted(userId));
        Optional<AppUser> userOptional = userService.getUserById(userId);
        if (userOptional.isPresent()) {
            AppUser appUser = userOptional.get();
            UserView userView = new UserView(appUser.getId(), appUser.getUsername());
            logResponse("user view");
            return ResponseEntity.ok().body(userView);
        } else {
            throw new UserNotFoundException();
        }
    }

    @PostMapping(path = "login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        logReceivedRequest("log in %s".formatted(loginRequest.getUsername()));
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
        UserStatusView status = new UserStatusView(appUser.getId(), "online");
        if (!statusServiceClient.setUserStatus(status)) {
            System.err.println("Failed to set status to online for user ID " + appUser.getId());
        }
        // Construct and return response
        LoginResponse loginResponse = new LoginResponse(token, new UserView(appUser.getId(), appUser.getUsername()));
        logResponse("user view and JWT");
        return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
    }

    @PostMapping(path = "{userId}/logout")
    public ResponseEntity<Void> logout(
            @PathVariable("userId") Long userId
    ) {
        logReceivedRequest("log out user %d".formatted(userId));
        // Validate user ID
        if (!userService.existsById(userId)) {
            throw new UserNotFoundException();
        }
        // Set user status to offline
        UserStatusView status = new UserStatusView(userId, "offline");
        if (!statusServiceClient.setUserStatus(status)) {
            System.err.println("Failed to set status to offline for user ID " + userId);
        }
        logResponse("status 200");
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "authenticate")
    public ResponseEntity<?> authenticate(HttpServletRequest request) {
        logReceivedRequest("authenticate request");
        HttpStatus status =  HttpStatus.OK;
        String token = jwtProvider.getJwtFromRequest(request);
        if (token == null || !jwtProvider.isValidToken(token)) {
            status = HttpStatus.UNAUTHORIZED;
        }
        logResponse("status 200");
        return new ResponseEntity<>(status);
    }

    @GetMapping(path = "by/username/{username}")
    public ResponseEntity<UserView> getUserByUsername(@PathVariable("username") String username) {
        logReceivedRequest("get user with username %s".formatted(username));
        AppUser user = userService.getUserByUsername(username)
                .orElseThrow(UserNotFoundException::new);
        UserView userView = new UserView(user.getId(), user.getUsername());
        logResponse("user view");
        return ResponseEntity.ok().body(userView);
    }

    @GetMapping(path = "{userId}/exists")
    public ResponseEntity<?> userExists(@PathVariable("userId") Long userId) {
        logReceivedRequest("check if user %d exists".formatted(userId));
        if (userService.existsById(userId)) {
            logResponse("status 200");
            return ResponseEntity.ok().build();
        } else {
            logResponse("status 400");
            return ResponseEntity.badRequest().build();
        }
    }
}
