package com.callme.services.friendservice.service;

import com.callme.services.friendservice.exception.*;
import com.callme.services.friendservice.model.FriendRelationship;
import com.callme.services.friendservice.model.Invitation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FriendService {
    public boolean areFriends(Long user1, Long user2) throws UserNotFoundException;
    public Invitation createInvitation(Invitation invitation) throws DuplicateRelationshipException, SelfRelationshipException, UserNotFoundException;
    public void acceptInvitation(Long id) throws InvitationNotFoundException;
    public void declineInvitation(Long id) throws InvitationNotFoundException;
    public List<FriendRelationship> findFriendsByUserId(Long userId) throws UserNotFoundException;
    public List<Invitation> findInvitationsByInviter(Long inviter) throws UserNotFoundException;
    public List<Invitation> findInvitationsByInvitee(Long invitee) throws UserNotFoundException;
}
