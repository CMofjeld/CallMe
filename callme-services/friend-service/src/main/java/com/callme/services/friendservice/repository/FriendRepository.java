package com.callme.services.friendservice.repository;

import com.callme.services.friendservice.model.FriendRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<FriendRelationship, Long> {
    public Optional<FriendRelationship> findById(Long id);
    public List<FriendRelationship> findByInviterOrInvitee(Long inviter, Long invitee);
    public boolean existsByInviterAndInvitee(Long inviter, Long invitee);
    public List<FriendRelationship> findByInviterAndInvitee(Long inviter, Long invitee);
}
