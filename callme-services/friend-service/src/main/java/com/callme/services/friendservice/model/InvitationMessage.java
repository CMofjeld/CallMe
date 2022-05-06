package com.callme.services.friendservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvitationMessage {
    @NotNull
    private Long id;
    @NotNull
    Long inviter;
    @NotNull
    Long invitee;
    @NotEmpty
    String status;

    public InvitationMessage(Invitation invitation, String status) {
        this.id = invitation.getId();
        this.inviter = invitation.getInviter();
        this.invitee = invitation.getInvitee();
        this.status = status;
    }
}
