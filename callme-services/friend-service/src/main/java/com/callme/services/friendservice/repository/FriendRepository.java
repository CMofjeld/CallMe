package com.callme.services.friendservice.repository;

import com.callme.services.friendservice.model.FriendRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<FriendRelationship, Long> {
    public List<FriendRelationship> findByUserId(Long userId);
    public boolean existsByUserIdAndFriendId(Long userId, Long friendId);
}
