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

    private void logRequest(String requestAction) {
        System.out.println("Received request to " + requestAction);
    }

    private void logResponse(String responseContent) {
        System.out.println("Returning response with " + responseContent);
    }

    @PostMapping(path = "invitation")
    public ResponseEntity<Invitation> createInvitation(
            @Valid @RequestBody Invitation invitation
    ) throws DuplicateRelationshipException, SelfRelationshipException, UserNotFoundException {
        logRequest("send a friend invitation from user %d to user %d".formatted(
                invitation.getInviter(),
                invitation.getInvitee()
        ));
        Invitation createdInvitation = friendService.createInvitation(invitation);
        logResponse("created invitation");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdInvitation);
    }

    @GetMapping(path = "user/{userId}")
    public ResponseEntity<List<FriendRelationship>> findRelationshipsByUser(
            @PathVariable("userId") Long userId
    ) throws UserNotFoundException {
        logRequest("get friends for user %d".formatted(userId));
        List<FriendRelationship> friends = friendService.findFriendsByUserId(userId);
        logResponse("list of friends");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(friends);
    }

    @PostMapping(path = "invitation/{invitationId}/accept")
    public ResponseEntity<Void> acceptInvitation(
            @PathVariable("invitationId") Long invitationId
    ) throws InvitationNotFoundException {
        logRequest("accept invitation %d".formatted(invitationId));
        friendService.acceptInvitation(invitationId);
        logResponse("status 200");
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
        logRequest("get outbound invitations for user %d".formatted(userId));
        return ResponseEntity
                .ok()
                .body(friendService.findInvitationsByInviter(userId));
    }

    @GetMapping(path = "invitation/user/{userId}/incoming")
    public ResponseEntity<List<Invitation>> getIncomingInvitations(
            @PathVariable("userId") Long userId
    ) throws UserNotFoundException {
        logRequest("get incoming invitations for user %d".formatted(userId));
        return ResponseEntity
                .ok()
                .body(friendService.findInvitationsByInvitee(userId));
    }

    @PostMapping(path = "are_friends")
    public ResponseEntity<Void> areFriends(
            @Valid @RequestBody FriendQuery friendQuery
    ) throws UserNotFoundException {
        logRequest("check if user %d and user %d are friends".formatted(
                friendQuery.getUser1(),
                friendQuery.getUser2()
        ));
        boolean success = friendService.areFriends(friendQuery.getUser1(), friendQuery.getUser2());
        if (success) {
            logResponse("status 200");
            return ResponseEntity.ok().build();
        } else {
            logResponse("status 400");
            return ResponseEntity.badRequest().build();
        }
    }
}
