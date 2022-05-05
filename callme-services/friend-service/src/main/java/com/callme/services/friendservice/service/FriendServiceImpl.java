package com.callme.services.friendservice.service;

import com.callme.services.common.service.UserServiceClient;
import com.callme.services.friendservice.exception.DuplicateRelationshipException;
import com.callme.services.friendservice.exception.SelfRelationshipException;
import com.callme.services.friendservice.exception.UserNotFoundException;
import com.callme.services.friendservice.messaging.MessagePublisher;
import com.callme.services.friendservice.model.FriendRelationship;
import com.callme.services.friendservice.model.RelationshipStatus;
import com.callme.services.friendservice.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

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
    public FriendRelationship save(FriendRelationship relationship) throws DuplicateRelationshipException, SelfRelationshipException {
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
        relationship.setStatus(RelationshipStatus.PENDING);
        return friendRepository.save(relationship);
    }
}
