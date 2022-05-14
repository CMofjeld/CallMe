package com.callme.services.statusservice.controller;

import com.callme.services.common.model.UserStatusView;
import com.callme.services.statusservice.model.UserStatus;
import com.callme.services.statusservice.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping(path = "status")
@RequiredArgsConstructor
public class UserStatusController {
    private final UserStatusService userStatusService;

    @PostMapping
    public ResponseEntity<Void> setUserStatus(@RequestBody @Valid UserStatus userStatus) {
        HttpStatus status = userStatusService.saveUserStatus(userStatus) ? HttpStatus.CREATED : HttpStatus.NOT_FOUND;
        return new ResponseEntity(status);
    }

    @GetMapping(path = "{userId}")
    public ResponseEntity<UserStatusView> getUserStatus(@PathVariable("userId") Long userId) {
        Optional<UserStatus> optionalUserStatus = userStatusService.getUserStatusById(userId);
        if (optionalUserStatus.isPresent()) {
            UserStatus userStatus = optionalUserStatus.get();
            return ResponseEntity.ok().body(new UserStatusView(userStatus.getId(), userStatus.getStatus()));
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }
}
