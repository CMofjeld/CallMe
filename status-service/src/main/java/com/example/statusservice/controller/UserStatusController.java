package com.example.statusservice.controller;

import com.example.statusservice.model.UserStatus;
import com.example.statusservice.service.UserStatusService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<UserStatus> getUserStatus(@PathVariable("userId") String userId) {
        Optional<UserStatus> optionalUserStatus = userStatusService.getUserStatusById(userId);
        if (optionalUserStatus.isPresent()) {
            return ResponseEntity.ok().body(optionalUserStatus.get());
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }
}
