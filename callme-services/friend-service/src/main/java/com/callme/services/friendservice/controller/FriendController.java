package com.callme.services.friendservice.controller;

import com.callme.services.friendservice.exception.DuplicateRelationshipException;
import com.callme.services.friendservice.exception.SelfRelationshipException;
import com.callme.services.friendservice.exception.UserNotFoundException;
import com.callme.services.friendservice.model.FriendRelationship;
import com.callme.services.friendservice.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(path = "friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

    @PostMapping
    public ResponseEntity<FriendRelationship> createRelationship(@Valid @RequestBody FriendRelationship relationship) throws DuplicateRelationshipException, SelfRelationshipException, UserNotFoundException {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(friendService.save(relationship));
    }

    @GetMapping(path = "user/{userId}")
    public ResponseEntity<List<FriendRelationship>> findRelationshipsByUser(@PathVariable("userId") Long userId) throws UserNotFoundException {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(friendService.findByUserId(userId));
    }
}
