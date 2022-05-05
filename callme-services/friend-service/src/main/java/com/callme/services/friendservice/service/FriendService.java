package com.callme.services.friendservice.service;

import com.callme.services.friendservice.exception.DuplicateRelationshipException;
import com.callme.services.friendservice.exception.SelfRelationshipException;
import com.callme.services.friendservice.exception.UserNotFoundException;
import com.callme.services.friendservice.model.FriendRelationship;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FriendService {
    boolean relationshipExistsBetween(Long user1, Long user2);
    FriendRelationship save(FriendRelationship relationship) throws DuplicateRelationshipException, SelfRelationshipException, UserNotFoundException;
    List<FriendRelationship> findByUserId(Long userId) throws UserNotFoundException;
}
