package com.callme.services.friendservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Invitation {
    @Id
    @SequenceGenerator(
            name = "invitation_sequence",
            sequenceName = "invitation_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "invitation_sequence"
    )
    private Long id;
    @NotNull
    Long inviter;
    @NotNull
    Long invitee;
}
