package com.callme.services.friendservice.service;

import com.callme.services.common.service.UserServiceClient;
import com.callme.services.friendservice.exception.DuplicateRelationshipException;
import com.callme.services.friendservice.exception.SelfRelationshipException;
import com.callme.services.friendservice.exception.UserNotFoundException;
import com.callme.services.friendservice.messaging.MessagePublisher;
import com.callme.services.friendservice.model.FriendRelationship;
import com.callme.services.friendservice.model.RelationshipStatus;
import com.callme.services.friendservice.repository.FriendRepository;
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


    @Override
    public boolean relationshipExistsBetween(Long user1, Long user2) {
        return friendRepository.existsByInviterAndInvitee(user1, user2) ||
                friendRepository.existsByInviterAndInvitee(user2, user1);
    }

    @Override
    @Transactional
    public FriendRelationship save(FriendRelationship relationship) throws DuplicateRelationshipException, SelfRelationshipException, UserNotFoundException {
        // Validate relationship
        if (!userServiceClient.userExists(relationship.getInvitee()) ||
            !userServiceClient.userExists(relationship.getInviter())) {
            throw new UserNotFoundException();
        }
        if (relationship.getInviter().equals(relationship.getInvitee())) {
            throw new SelfRelationshipException();
        }
        if (relationshipExistsBetween(relationship.getInviter(), relationship.getInvitee())) {
            throw new DuplicateRelationshipException();
        }
        // Save the relationship
        relationship.setStatus(RelationshipStatus.PENDING);
        FriendRelationship createdRelationship = friendRepository.save(relationship);
        // Publish messages to notify both parties
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(createdRelationship);
            messagePublisher.publish("friends." + relationship.getInviter(), jsonMessage);
            messagePublisher.publish("friends." + relationship.getInvitee(), jsonMessage);
        } catch (Exception e) {
            System.err.println(e);
        }
        // Return the newly created relationship
        return createdRelationship;
    }

    @Override
    public List<FriendRelationship> findByUserId(Long userId) throws UserNotFoundException {
        // Validate user ID
        if (!userServiceClient.userExists(userId)) {
            throw new UserNotFoundException();
        }
        // Find relationships where the user is either the inviter or invitee
        return friendRepository.findByInviterOrInvitee(userId, userId);
    }
}
