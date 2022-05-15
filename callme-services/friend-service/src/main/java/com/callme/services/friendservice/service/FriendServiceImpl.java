package com.callme.services.friendservice.service;

import com.callme.services.common.model.UserStatusView;
import com.callme.services.common.service.UserServiceClient;
import com.callme.services.friendservice.exception.*;
import com.callme.services.friendservice.messaging.MessagePublisher;
import com.callme.services.friendservice.model.FriendRelationship;
import com.callme.services.friendservice.model.Invitation;
import com.callme.services.friendservice.model.InvitationMessage;
import com.callme.services.friendservice.model.RelationshipStatus;
import com.callme.services.friendservice.repository.FriendRepository;
import com.callme.services.friendservice.repository.InvitationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService{
    private final FriendRepository friendRepository;
    private final MessagePublisher messagePublisher;
    private final UserServiceClient userServiceClient;
    private final InvitationRepository invitationRepository;

    private void logDBQuery(String queryDescription) {
        System.out.println("Querying database to %s".formatted(queryDescription));
    }

    private void logDBStore(String storedObject) {
        System.out.println("Storing %s in database".formatted(storedObject));
    }

    private void logDBDelete(String storedObject) {
        System.out.println("Deleting %s from database".formatted(storedObject));
    }

    private void logPubMessage(String messageContent, String recipient) {
        System.out.println("Publishing message with %s to %s".formatted(messageContent, recipient));
    }

    @Override
    public boolean areFriends(Long user1, Long user2) throws UserNotFoundException {
        if (!userServiceClient.userExists(user1) || !userServiceClient.userExists(user2)) {
            throw new UserNotFoundException();
        }
        logDBQuery("check if user %d and user %d are friends".formatted(
                user1,
                user2
        ));
        return friendRepository.existsByUserIdAndFriendId(user1, user2);
    }

    @Override
    @Transactional
    public Invitation createInvitation(Invitation invitation) throws DuplicateRelationshipException, SelfRelationshipException, UserNotFoundException {
        Long invitee = invitation.getInvitee();
        Long inviter = invitation.getInviter();
        // Check for existing friendship or invitation
        logDBQuery("check for existing invitation between users");
        if (areFriends(invitee, inviter) ||
            invitationRepository.existsByInviteeAndInviter(inviter, invitee) ||
            invitationRepository.existsByInviteeAndInviter(invitee, inviter)) {
            throw new DuplicateRelationshipException();
        }
        // Check for self invitation
        if (invitee.equals(inviter)) {
            throw new SelfRelationshipException();
        }
        // Save invitation
        logDBStore("new invitation");
        Invitation savedInvitation = invitationRepository.save(invitation);
        // Publish messages
        publishInvitationMessage(new InvitationMessage(savedInvitation, "pending"), invitation.getInvitee());
        publishInvitationMessage(new InvitationMessage(savedInvitation, "pending"), invitation.getInviter());
        // Return saved invitation
        return savedInvitation;
    }

    @Override
    @Transactional
    public void acceptInvitation(Long id) throws InvitationNotFoundException {
        // Find the invitation
        logDBQuery("retrieve invitation %d".formatted(id));
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(InvitationNotFoundException::new);
        Long inviter = invitation.getInviter();
        Long invitee = invitation.getInvitee();
        // Create friend relationships
        FriendRelationship relationship1 = new FriendRelationship();
        relationship1.setUserId(inviter);
        relationship1.setFriendId(invitee);
        logDBStore("new friend relationship");
        friendRepository.save(relationship1);
        FriendRelationship relationship2 = new FriendRelationship();
        relationship2.setUserId(invitee);
        relationship2.setFriendId(inviter);
        friendRepository.save(relationship2);
        // Delete the invitation
        logDBDelete("invitation %d".formatted(id));
        invitationRepository.delete(invitation);
        // Publish messages
        InvitationMessage invitationMessage = new InvitationMessage(invitation, "accepted");
        publishInvitationMessage(invitationMessage, inviter);
        publishInvitationMessage(invitationMessage, invitee);
        publishFriendshipMessage(relationship1, inviter);
        publishFriendshipMessage(relationship2, invitee);
    }

    @Override
    public void declineInvitation(Long id) throws InvitationNotFoundException {
        // Find the invitation
        logDBQuery("retrieve invitation %d".formatted(id));
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(InvitationNotFoundException::new);
        Long inviter = invitation.getInviter();
        Long invitee = invitation.getInvitee();
        // Delete the invitation
        logDBDelete("invitation %d".formatted(id));
        invitationRepository.delete(invitation);
        // Publish messages
        InvitationMessage invitationMessage = new InvitationMessage(invitation, "declined");
        publishInvitationMessage(invitationMessage, inviter);
        publishInvitationMessage(invitationMessage, invitee);
    }

    @Override
    public List<FriendRelationship> findFriendsByUserId(Long userId) throws UserNotFoundException {
        if (!userServiceClient.userExists(userId)) {
            throw new UserNotFoundException();
        }
        return friendRepository.findByUserId(userId);
    }

    @Override
    public List<Invitation> findInvitationsByInviter(Long inviter) throws UserNotFoundException {
        if (!userServiceClient.userExists(inviter)) {
            throw new UserNotFoundException();
        }
        logDBQuery("get outbound invitations for user %d".formatted(inviter));
        return invitationRepository.findByInviter(inviter);
    }

    @Override
    public List<Invitation> findInvitationsByInvitee(Long invitee) throws UserNotFoundException {
        if (!userServiceClient.userExists(invitee)) {
            throw new UserNotFoundException();
        }
        logDBQuery("get incoming invitations for user %d".formatted(invitee));
        return invitationRepository.findByInvitee(invitee);
    }

    private void publishInvitationMessage(InvitationMessage message, Long recipient) {
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(message);
            logPubMessage("updated invitation", "user %d".formatted(recipient));
            messagePublisher.publish("invitations." + recipient, jsonMessage);
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing user status to JSON.");
            System.err.println(e.toString());
        }
    }

    private void publishFriendshipMessage(FriendRelationship relationship, Long recipient) {
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(relationship);
            logPubMessage("updated friendsip", "user %d".formatted(recipient));
            messagePublisher.publish("friends." + recipient, jsonMessage);
        } catch (JsonProcessingException e) {
            System.err.println("Error serializing user status to JSON.");
            System.err.println(e.toString());
        }
    }
}
