package com.callme.services.userservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
public class AppUser {
    @Id
    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_sequence"
    )
    private Long id;
    @NotBlank
    @Column(unique = true)
    String username;
    @NotBlank
    String password;
    UserRole role;

    public AppUser(String username, String password) {
        this.username = username;
        this.password = password;
        this.role = UserRole.APP_USER;
    }
}
