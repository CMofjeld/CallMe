package com.callme.services.friendservice.service;

import com.callme.services.friendservice.exception.*;
import com.callme.services.friendservice.model.FriendRelationship;
import com.callme.services.friendservice.model.RelationshipStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FriendService {
    public boolean relationshipExistsBetween(Long user1, Long user2);
    public FriendRelationship save(FriendRelationship relationship) throws DuplicateRelationshipException, SelfRelationshipException, UserNotFoundException;
    public List<FriendRelationship> findByUserId(Long userId) throws UserNotFoundException;
    public void updateRelationshipStatus(Long id, RelationshipStatus newStatus) throws RelationshipNotFoundException, InvalidRelationshipStatusException;
}
