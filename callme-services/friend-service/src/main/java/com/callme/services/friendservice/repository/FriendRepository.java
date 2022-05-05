package com.callme.services.friendservice.repository;

import com.callme.services.friendservice.model.FriendRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<FriendRelationship, Long> {
    List<FriendRelationship> findByInviterOrInvitee(Long inviter, Long invitee);
    boolean existsByInviterAndInvitee(Long inviter, Long invitee);
    List<FriendRelationship> findByInviterAndInvitee(Long inviter, Long invitee);
}
