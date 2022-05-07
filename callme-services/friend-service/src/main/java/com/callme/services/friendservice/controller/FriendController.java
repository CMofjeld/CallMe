package com.callme.services.friendservice.controller;

import com.callme.services.friendservice.exception.*;
import com.callme.services.friendservice.model.FriendQuery;
import com.callme.services.friendservice.model.FriendRelationship;
import com.callme.services.friendservice.model.Invitation;
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

    @PostMapping(path = "invitation")
    public ResponseEntity<Invitation> createInvitation(
            @Valid @RequestBody Invitation invitation
    ) throws DuplicateRelationshipException, SelfRelationshipException, UserNotFoundException {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(friendService.createInvitation(invitation));
    }

    @GetMapping(path = "user/{userId}")
    public ResponseEntity<List<FriendRelationship>> findRelationshipsByUser(
            @PathVariable("userId") Long userId
    ) throws UserNotFoundException {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(friendService.findFriendsByUserId(userId));
    }

    @PostMapping(path = "invitation/{invitationId}/accept")
    public ResponseEntity<Void> acceptInvitation(
            @PathVariable("invitationId") Long invitationId
    ) throws InvitationNotFoundException {
        friendService.acceptInvitation(invitationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "invitation/{invitationId}/decline")
    public ResponseEntity<Void> declineInvitation(
            @PathVariable("invitationId") Long invitationId
    ) throws InvitationNotFoundException {
        friendService.declineInvitation(invitationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "invitation/user/{userId}/outgoing")
    public ResponseEntity<List<Invitation>> getOutgoingInvitations(
            @PathVariable("userId") Long userId
    ) throws UserNotFoundException {
        return ResponseEntity
                .ok()
                .body(friendService.findInvitationsByInviter(userId));
    }

    @GetMapping(path = "invitation/user/{userId}/incoming")
    public ResponseEntity<List<Invitation>> getIncomingInvitations(
            @PathVariable("userId") Long userId
    ) throws UserNotFoundException {
        return ResponseEntity
                .ok()
                .body(friendService.findInvitationsByInvitee(userId));
    }

    @GetMapping(path = "are_friends")
    public ResponseEntity<Void> areFriends(
            @Valid @RequestBody FriendQuery friendQuery
    ) throws UserNotFoundException {
        return friendService.areFriends(friendQuery.getUser1(), friendQuery.getUser2()) ?
                ResponseEntity.ok().build() :
                ResponseEntity.badRequest().build();
    }
}
