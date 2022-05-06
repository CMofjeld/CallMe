package com.callme.services.friendservice.repository;

import com.callme.services.friendservice.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    public Optional<Invitation> findById(Long id);
    public List<Invitation> findByInvitee(Long invitee);
    public List<Invitation> findByInviter(Long inviter);
    public boolean existsByInviteeAndInviter(Long invitee, Long inviter);
}
