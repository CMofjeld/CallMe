package com.callme.services.friendservice.controller;

import com.callme.services.friendservice.exception.DuplicateRelationshipException;
import com.callme.services.friendservice.exception.SelfRelationshipException;
import com.callme.services.friendservice.model.FriendRelationship;
import com.callme.services.friendservice.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@CrossOrigin
@RequestMapping(path = "friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

    @PostMapping
    public ResponseEntity<FriendRelationship> createRelationship(@Valid @RequestBody FriendRelationship relationship) throws DuplicateRelationshipException, SelfRelationshipException {
        return ResponseEntity.status(HttpStatus.CREATED).body(friendService.save(relationship));
    }
}
